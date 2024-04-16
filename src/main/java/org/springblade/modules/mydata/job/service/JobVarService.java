package org.springblade.modules.mydata.job.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.manage.cache.EnvVarCache;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.entity.EnvVar;
import org.springblade.modules.mydata.manage.service.IEnvService;
import org.springblade.modules.mydata.manage.service.IEnvVarService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 任务的变量处理类
 *
 * @author LIEN
 * @since 2023/11/5
 */
@Component
public class JobVarService {

    @Resource
    private IEnvVarService envVarService;

    @Resource
    private IEnvService envService;

    // 用户自定义变量 ${} 的正则表达式
    private static final String USER_VAR_PATTERN = "\\$\\{([^}]*)\\}";

    // 系统内置变量 {$} 的正则表达式
    private static final String SYS_VAR_PATTERN = "\\{\\$([^}]*)\\}";

    private static final String DATA_FIELD_PATTERN = "\\{([^}]*)\\}";

    /**
     * 将json中提取指定数据 保存到任务的指定环境变量
     *
     * @param task       任务
     * @param jsonString json数据
     */
    public void saveVarValue(TaskInfo task, String jsonString) {
        if (task == null || StrUtil.isEmpty(jsonString)) {
            return;
        }

        // 接口字段 与 变量的映射
        Map<String, String> fieldVarMapping = task.getFieldVarMapping();
        if (CollUtil.isEmpty(fieldVarMapping)) {
            return;
        }

        JSON json = JSONUtil.parse(jsonString);
        fieldVarMapping.forEach((apiField, varName) -> {
            String varValue = json.getByPath(apiField, String.class);
            Long envId = task.getEnvId();

            EnvVar envVar = new EnvVar();
            envVar.setEnvId(envId);
            envVar.setVarName(varName);
            envVar.setVarValue(varValue);
            envVar.setTenantId(task.getTenantId());

            envVarService.saveByNameInEnv(envVar);
            task.appendLog("保存环境变量，tenantId：{}，varName：{}，varValue：{}", envVar.getTenantId(), envVar.getVarName(), envVar.getVarValue());
        });

    }

    /**
     * 解析任务API header和param中的变量表达式，从任务对应环境中获取变量值 并替换变量；
     *
     * @param taskInfo 任务
     */
    public void parseVar(TaskInfo taskInfo) {
        Set<String> userVarNames = CollUtil.newHashSet();

        // 从API的header和param中 解析变量表达式
        Map<String, String> reqHeaders = taskInfo.getReqHeaders();
        Map<String, Object> reqParams = taskInfo.getReqParams();

        if (CollUtil.isNotEmpty(reqHeaders)) {
            //varNames.addAll(MdUtil.parseVarNames(reqHeaders.keySet()));
            // 替换header中的系统内置变量
            replaceSysVarValues(reqHeaders);
            // 提取用户自定义变量名
            userVarNames.addAll(parseUserVarNames(reqHeaders.values()));
        }
        if (CollUtil.isNotEmpty(reqParams)) {
            //varNames.addAll(MdUtil.parseVarNames(reqParams.keySet()));
            // 替换param中的系统内置变量
            replaceSysVarValues(reqParams);
            // 提取用户自定义变量名
            userVarNames.addAll(parseUserVarNames(reqParams.values()));
        }
        // 若没有变量名，则结束解析
        if (CollUtil.isEmpty(userVarNames)) {
            return;
        }

        // 根据变量名 获取环境变量值
        Long envId = taskInfo.getEnvId();
        List<EnvVar> envVars = CollUtil.newArrayList();
        Env env = envService.getById(envId);

        // redis中没有缓存 需要查数据库的变量名
        if (env != null && CollUtil.isNotEmpty(userVarNames)) {
            userVarNames.forEach(varName -> {
                // 尝试从redis获取变量
                EnvVar envVar = EnvVarCache.getEnvVar(env.getTenantId(), envId, varName);
                if (envVar != null) {
                    // 替换环境变量值中的系统内置变量
                    envVar.setVarValue(replaceSysVarValue(envVar.getVarValue()));
                    // 环境变量存入返回结果列表
                    envVars.add(envVar);
                }
            });
        }

        // 将环境变量转化为key:value格式
        Map<String, String> varMap = envVars.stream()
                .collect(Collectors.toMap(EnvVar::getVarName, EnvVar::getVarValue));

        taskInfo.appendLog("解析出用户变量：{}", varMap);

        // 替换 header和param 中的变量
        if (CollUtil.isNotEmpty(reqHeaders)) {
            taskInfo.setReqHeaders(replaceUserVarValues(reqHeaders, varMap));
        }
        if (CollUtil.isNotEmpty(reqParams)) {
            taskInfo.setReqParams(replaceUserVarValues(reqParams, varMap));
        }
    }

    /**
     * 解析url中的 数据字段变量 并替换数据
     *
     * @param taskInfo 任务
     */
    public void parseConsumeUrlVar(TaskInfo taskInfo) {
        // api地址
        String apiUrl = taskInfo.getApiUrl();
        // 解析{field}格式的变量名
        List<String> fieldNames = parseVarNames(apiUrl, DATA_FIELD_PATTERN, "{", "}");
        // 若解析为空，则结束
        if (CollUtil.isEmpty(fieldNames)) {
            return;
        }
        // 提取第一条消费数据
        Map data = taskInfo.getConsumeDataList().get(0);
        // 替换映射
        Map<String, String> replaceMap = MapUtil.newHashMap();
        for (String field : fieldNames) {
            if (!data.containsKey(field)) {
                continue;
            }
            // 从数据中 取出数据 并存入替换映射
            String value = ObjectUtil.toString(data.remove(field));
            replaceMap.put(field, value);
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(replaceMap);
        stringSubstitutor.setVariablePrefix("{");
        stringSubstitutor.setVariableSuffix("}");
        // 替换变量值
        apiUrl = stringSubstitutor.replace(apiUrl);
        taskInfo.setApiUrl(apiUrl);
    }

    /**
     * 替换用户自定义变量 ${var}
     *
     * @param sourceMap 替换前的map数据
     * @param varMap    变量名-变量值
     * @return 替换后的数据
     */
    private <V> Map<String, V> replaceUserVarValues(Map<String, V> sourceMap, Map<String, String> varMap) {
        Map<String, V> resultMap = MapUtil.newHashMap();
        StringSubstitutor stringSubstitutor = new StringSubstitutor(varMap);
        sourceMap.forEach((k, v) -> {
            // 替换用户自定义变量
            resultMap.put(k, (V) stringSubstitutor.replace(v));
        });

        return resultMap;
    }

    /**
     * 从多个字符串中 解析所有${}表达式中的变量名
     *
     * @param strings 字符串集合
     * @return 变量名列表
     */
    private List<String> parseUserVarNames(Collection<?> strings) {
        List<String> list = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(strings)) {
            for (Object string : strings) {
                list.addAll(parseVarNames(string.toString(), USER_VAR_PATTERN, "${", "}"));
            }
        }
        return list;
    }

    /**
     * 从字符串中 解析指定表达式中的变量名
     *
     * @param string  字符串
     * @param pattern 表达式
     * @return 变量名列表
     */
    public static List<String> parseVarNames(String string, String pattern, String prefix, String suffix) {
        if (StrUtil.isEmpty(string)) {
            return CollUtil.newArrayList();
        }

        List<String> varNames = ReUtil.findAll(pattern, string, 0);
        if (CollUtil.isNotEmpty(varNames)) {
            ListIterator<String> iterator = varNames.listIterator();
            while (iterator.hasNext()) {
                String varName = iterator.next();
                varName = getKey(varName, prefix.length(), suffix.length());
                iterator.set(varName);
            }
        }
        return varNames;
    }

    /**
     * 解析处理系统内置变量 {$var}
     *
     * @param string 被解析的字符串
     * @return 替换后的字符串
     */
    private String replaceSysVarValue(String string) {
        // 解析系统内置变量
        List<String> sysVarNames = parseVarNames(string, SYS_VAR_PATTERN, "{$", "}");
        if (CollUtil.isEmpty(sysVarNames)) {
            return string;
        }

        Map<String, String> replaceMap = MapUtil.newHashMap();
        for (String sysVarName : sysVarNames) {
            switch (sysVarName) {
                case "timestamp":
                    long timestamp = DateUtil.current();
                    replaceMap.put("timestamp", String.valueOf(timestamp));
                    break;
                case "timestamp_second":
                    long second = DateUtil.currentSeconds();
                    replaceMap.put("timestamp_second", String.valueOf(second));
                    break;
            }
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(replaceMap);
        stringSubstitutor.setVariablePrefix("{$");
        stringSubstitutor.setVariableSuffix("}");
        return stringSubstitutor.replace(string);
    }

    private <V> void replaceSysVarValues(Map<String, V> map) {
        if (MapUtil.isEmpty(map)) {
            return;
        }

        map.forEach((k, v) -> {
            // 替换用户自定义变量
            map.put(k, (V) replaceSysVarValue(v.toString()));
        });
    }

    private static String getKey(String g, int prefix, int suffix) {
        return g.substring(prefix, g.length() - suffix);
    }
}
