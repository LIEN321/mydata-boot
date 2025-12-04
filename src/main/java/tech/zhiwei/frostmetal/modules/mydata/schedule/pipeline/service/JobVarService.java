package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import cn.hutool.core.util.ReUtil;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.StringUtil;
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
     * 系统内置变量 {{sys_var}}
     */
    private static final String SYS_VAR_PATTERN = "\\{\\{([^}]*)\\}\\}";
    private static final String SYS_VAR_PATTERN_PREFIX = "{{";
    private static final String SYS_VAR_PATTERN_SUFFIX = "}}";

    /**
     * 接口数据属性 ${field}
     */
    private static final String DATA_FIELD_PATTERN = "\\$\\{([^}]*)\\}";
    private static final String DATA_FIELD_PATTERN_PREFIX = "${";
    private static final String DATA_FIELD_PATTERN_SUFFIX = "}";

    /**
     * 仓库已有数据字段 {$field}
     */
    private static final String EXISTED_DATA_FIELD_PATTERN = "\\{\\$([^}]*)\\}";
    private static final String EXISTED_DATA_FIELD_PATTERN_PREFIX = "{$";
    private static final String EXISTED_DATA_FIELD_PATTERN_SUFFIX = "}";

    /**
     * 解析处理map值中的系统内置变量
     *
     * @param map Map对象
     */
    public static void processSysVarValues(Map<String, String> map) {
        if (MapUtil.isEmpty(map)) {
            return;
        }

        map.forEach((k, v) -> {
            // 替换用户自定义变量
            map.put(k, processSysVarValue(v));
        });
    }

    /**
     * 解析处理系统内置变量 {{var}}
     *
     * @param string 被解析的字符串
     * @return 替换后的字符串
     */
    public static String processSysVarValue(String string) {
        // 解析系统内置变量
        List<String> sysVarNames = parseVarNames(string, SYS_VAR_PATTERN, SYS_VAR_PATTERN_PREFIX, SYS_VAR_PATTERN_SUFFIX);
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
        stringSubstitutor.setVariablePrefix(SYS_VAR_PATTERN_PREFIX);
        stringSubstitutor.setVariableSuffix(SYS_VAR_PATTERN_SUFFIX);
        return stringSubstitutor.replace(string);
    }

    /**
     * 解析处理 字符串中 {$field} 格式的数据变量
     *
     * @param string  字符串
     * @param bizData 数据
     * @return 解析后的字符串
     */
    public static String processExistedDataVar(String string, Map<String, Object> bizData, Map<String, String> fieldTypeMapping) {
        return processDataVar(string, bizData, fieldTypeMapping, EXISTED_DATA_FIELD_PATTERN, EXISTED_DATA_FIELD_PATTERN_PREFIX, EXISTED_DATA_FIELD_PATTERN_SUFFIX);
    }


    public static void processDataFieldVar(Map<String, String> map, Map<String, Object> data) {
        processDataFieldVar(map, data, null);
    }

    /**
     * 解析 字符串中${field}格式的数据变量
     *
     * @param map
     * @param data 数据
     * @return 解析后的字符串
     */
    public static void processDataFieldVar(Map<String, String> map, Map<String, Object> data, Map<String, String> fieldTypeMapping) {
        if (MapUtil.isEmpty(map)) {
            return;
        }

        map.forEach((k, v) -> {
            // 替换用户自定义变量
            map.put(k, processDataVar(v, data, fieldTypeMapping, DATA_FIELD_PATTERN, DATA_FIELD_PATTERN_PREFIX, DATA_FIELD_PATTERN_SUFFIX));
        });
    }

    public static String processDataFieldVar(String string, Map<String, Object> data) {
        return processDataFieldVar(string, data, null);
    }

    /**
     * 解析 字符串中${field}格式的数据变量
     *
     * @param string 字符串
     * @param data   数据
     * @return 解析后的字符串
     */
    public static String processDataFieldVar(String string, Map<String, Object> data, Map<String, String> fieldTypeMapping) {
        return processDataVar(string, data, fieldTypeMapping, DATA_FIELD_PATTERN, DATA_FIELD_PATTERN_PREFIX, DATA_FIELD_PATTERN_SUFFIX);
    }

    private static String processDataVar(String string, Map<String, Object> bizData, Map<String, String> fieldTypeMapping, String pattern, String prefix, String suffix) {
        if (StringUtil.isEmpty(string)) {
            return string;
        }
        // 解析字符串中的属性变量名 {{field}}
        List<String> fieldNames = parseVarNames(string, pattern, prefix, suffix);
        // 若解析为空，则结束
        if (CollectionUtil.isEmpty(fieldNames)) {
            return string;
        }
        // 替换映射
        Map<String, String> replaceMap = MapUtil.newHashMap();
        for (String field : fieldNames) {
            String value;
            if (MapUtil.isNotEmpty(fieldTypeMapping) && fieldTypeMapping.containsKey(field)) {
                // 尝试获取数据的类型，若没有则默认为字符串
                String targetType = fieldTypeMapping.get(field);

                if (MapUtil.isEmpty(bizData) || !bizData.containsKey(field)) {
                    value = MyDataUtil.defaultValue(targetType);
                } else {
                    // 从数据中 取出数据 并存入替换映射
                    value = MyDataUtil.formatData(bizData.get(field), targetType);
                }
            } else {
                // 支持${var}和${obj.prop}表达式
                value = StringUtil.toStringOrNull(BeanUtil.getProperty(bizData, field));
            }
//            if (value == null) {
//                 throw new IllegalArgumentException(StringUtil.format("无法获取参数 {} 的值", field));
//            }
            replaceMap.put(field, value);
        }

        StringSubstitutor stringSubstitutor = new StringSubstitutor(replaceMap);
        stringSubstitutor.setVariablePrefix(prefix);
        stringSubstitutor.setVariableSuffix(suffix);
        // 替换变量值
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
        if (StringUtil.isEmpty(string)) {
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

    /**
     * 字符串是否为 属性表达式 {{field}}
     *
     * @param string 字符串
     * @return true-是属性表达式，false-不是
     */
    public static boolean isFieldExp(String string) {
        return ReUtil.isMatch(DATA_FIELD_PATTERN, string);
    }

    private static String getKey(String g, int prefix, int suffix) {
        return g.substring(prefix, g.length() - suffix);
    }
}
