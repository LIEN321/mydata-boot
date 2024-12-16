package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 任务的变量处理类
 *
 * @author LIEN
 * @since 2024/12/16
 */
@Component
public class JobVarService {

    /**
     * 系统内置变量 {$sys_var}
     */
    private static final String SYS_VAR_PATTERN = "\\{\\$([^}]*)\\}";

    /**
     * 解析处理map值中的系统内置变量
     *
     * @param map Map对象
     */
    public void replaceSysVarValues(Map<String, String> map) {
        if (MapUtil.isEmpty(map)) {
            return;
        }

        map.forEach((k, v) -> {
            // 替换用户自定义变量
            map.put(k, replaceSysVarValue(v));
        });
    }

    /**
     * 解析处理系统内置变量 {$var}
     *
     * @param string 被解析的字符串
     * @return 替换后的字符串
     */
    public String replaceSysVarValue(String string) {
        // 解析系统内置变量
        List<String> sysVarNames = parseVarNames(string, SYS_VAR_PATTERN, "{$", "}");
        if (CollectionUtil.isEmpty(sysVarNames)) {
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

    /**
     * 从字符串中 解析指定表达式中的变量名
     *
     * @param string  字符串
     * @param pattern 表达式
     * @return 变量名列表
     */
    public static List<String> parseVarNames(String string, String pattern, String prefix, String suffix) {
        if (StrUtil.isEmpty(string)) {
            return CollectionUtil.newArrayList();
        }

        List<String> varNames = ReUtil.findAll(pattern, string, 0);
        if (CollectionUtil.isNotEmpty(varNames)) {
            ListIterator<String> iterator = varNames.listIterator();
            while (iterator.hasNext()) {
                String varName = iterator.next();
                varName = getKey(varName, prefix.length(), suffix.length());
                iterator.set(varName);
            }
        }
        return varNames;
    }

    private static String getKey(String g, int prefix, int suffix) {
        return g.substring(prefix, g.length() - suffix);
    }
}
