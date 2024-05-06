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
import java.util.*;
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
     * @param taskInfo   任务
     * @param jsonString json数据
     */
    public void saveVarValue(TaskInfo taskInfo, String jsonString) {
        if (taskInfo == null || StrUtil.isEmpty(jsonString)) {
            return;
        }

        // 接口字段 与 变量的映射
        Map<String, String> fieldVarMapping = taskInfo.getFieldVarMapping();
        if (CollUtil.isEmpty(fieldVarMapping)) {
            return;
        }

        JSON json = JSONUtil.parse(jsonString);
        fieldVarMapping.forEach((apiField, varName) -> {
            String varValue = json.getByPath(apiField, String.class);
            Long envId = taskInfo.getEnvId();

            EnvVar envVar = new EnvVar();
            envVar.setEnvId(envId);
            envVar.setVarName(varName);
            envVar.setVarValue(varValue);
            envVar.setTenantId(taskInfo.getTenantId());

            envVarService.saveByNameInEnv(envVar);
            taskInfo.appendLog("保存环境变量，tenantId：{}，varName：{}，varValue：{}", envVar.getTenantId(), envVar.getVarName(), envVar.getVarValue());
        });

    }

    /**
     * 从指定map的value中，解析 系统变量、用户环境变量
     *
     * @param map   解析源
     * @param envId 环境id
     */
    public <V> void parseVar(Map<String, V> map, Long envId) {
        if (CollUtil.isEmpty(map)) {
            return;
        }

        // 替换map中的系统内置变量
        replaceSysVarValues(map);

        // 提取用户自定义变量名
        Map<String, String> userVars = parseUserVar(map.values(), envId);
        if (MapUtil.isEmpty(userVars)) {
            return;
        }

        replaceUserVarValues(map, userVars);
    }

    /**
     * 从字符串集合中，提取用户环境变量
     *
     * @param strings 字符串集合
     * @param envId   环境id
     * @return 用户环境变量
     */
    public <V> Map<String, String> parseUserVar(Collection<V> strings, Long envId) {
        // 提取用户自定义变量名
        Set<String> userVarNames = parseUserVarNames(strings);

        // 若没有用户变量名，则结束解析
        if (CollUtil.isEmpty(userVarNames)) {
            return null;
        }

        // 根据变量名 获取环境变量值
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
        return envVars.stream().collect(Collectors.toMap(EnvVar::getVarName, EnvVar::getVarValue));
    }

    /**
     * 解析任务API header和param中的变量表达式，从任务对应环境中获取变量值 并替换变量；
     *
     * @param taskInfo 任务
     */
    public void parseTaskVar(TaskInfo taskInfo) {

        // 替换 header和param 中的变量
        Map<String, String> reqHeaders = taskInfo.getReqHeaders();
        Map<String, Object> reqParams = taskInfo.getReqParams();

        parseVar(reqHeaders, taskInfo.getEnvId());
        parseVar(reqParams, taskInfo.getEnvId());

        // 替换 body 中的变量
        if (StrUtil.isNotEmpty(taskInfo.getReqBody())) {
            String reqBody = replaceSysVarValue(taskInfo.getReqBody());
            Map<String, String> userVars = parseUserVar(CollUtil.toList(reqBody), taskInfo.getEnvId());
            if (MapUtil.isEmpty(userVars)) {
                return;
            }

            reqBody = replaceUserVarValues(reqBody, userVars);
            taskInfo.setReqBody(reqBody);
        }
    }

    /**
     * 解析url、header、param中的 数据字段变量 并替换数据
     *
     * @param taskInfo 任务
     */
    public static void parseDataFieldVar(TaskInfo taskInfo, Map data) {
        if (MapUtil.isEmpty(data)) {
            return;
        }
        // api地址
        String apiUrl = taskInfo.getApiUrl();
        // 替换属性变量值
        taskInfo.setApiUrl(parseDataFieldVar(apiUrl, data));

        // 解析param中的属性变量名
        Map<String, Object> reqParams = taskInfo.getReqParams();
        if (MapUtil.isNotEmpty(reqParams)) {
            reqParams.forEach((k, v) -> {
                reqParams.put(k, parseDataFieldVar(StrUtil.toString(v), data));
            });
        }

        // 解析header中的属性变量名
        Map<String, String> reqHeaders = taskInfo.getReqHeaders();
        if (MapUtil.isNotEmpty(reqHeaders)) {
            reqHeaders.forEach((k, v) -> {
                reqHeaders.put(k, parseDataFieldVar(v, data));
            });
        }
    }

    public static String parseDataFieldVar(String string, Map data) {
        if (MapUtil.isEmpty(data) || StrUtil.isEmpty(string)) {
            return string;
        }
        // 解析url中的属性变量名 {field}
        List<String> fieldNames = parseVarNames(string, DATA_FIELD_PATTERN, "{", "}");
        // 若解析为空，则结束
        if (CollUtil.isEmpty(fieldNames)) {
            return string;
        }
        // 替换映射
        Map<String, String> replaceMap = MapUtil.newHashMap();
        for (String field : fieldNames) {
            if (!data.containsKey(field)) {
                continue;
            }
            // 从数据中 取出数据 并存入替换映射
//            String value = ObjectUtil.toString(data.remove(field));
            String value = ObjectUtil.toString(data.get(field));
            replaceMap.put(field, value);
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(replaceMap);
        stringSubstitutor.setVariablePrefix("{");
        stringSubstitutor.setVariableSuffix("}");
        // 替换变量值
        return stringSubstitutor.replace(string);
    }

    /**
     * 字符串是否为 属性表达式 {field}
     *
     * @param string 字符串
     * @return true-是属性表达式，false-不是
     */
    public static boolean isFieldExp(String string) {
        return ReUtil.isMatch(DATA_FIELD_PATTERN, string);
    }

    /**
     * 替换用户自定义变量 ${var}
     *
     * @param sourceMap 替换前的map数据
     * @param varMap    变量名-变量值
     */
    private <V> void replaceUserVarValues(Map<String, V> sourceMap, Map<String, String> varMap) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(varMap);
        sourceMap.forEach((k, v) -> {
            // 替换用户自定义变量
            sourceMap.put(k, (V) stringSubstitutor.replace(v));
        });
    }

    /**
     * 替换用户自定义变量 ${var}
     *
     * @param string 字符串
     * @param varMap 变量名-变量值
     */
    private String replaceUserVarValues(String string, Map<String, String> varMap) {
        StringSubstitutor stringSubstitutor = new StringSubstitutor(varMap);
        // 替换用户自定义变量
        return stringSubstitutor.replace(string);
    }

    /**
     * 从多个字符串中 解析所有${}表达式中的变量名
     *
     * @param strings 字符串集合
     * @return 变量名列表
     */
    private Set<String> parseUserVarNames(Collection<?> strings) {
        Set<String> userVarNames = CollUtil.newHashSet();
        if (CollUtil.isNotEmpty(strings)) {
            for (Object string : strings) {
                userVarNames.addAll(parseVarNames(string.toString(), USER_VAR_PATTERN, "${", "}"));
            }
        }
        return userVarNames;
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
