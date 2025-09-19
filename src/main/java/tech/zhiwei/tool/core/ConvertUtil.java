package tech.zhiwei.tool.core;

import cn.hutool.core.convert.Convert;
import tech.zhiwei.tool.lang.StringUtil;

/**
 * 类型转换工具
 *
 * @author LIEN
 * @since 2025/9/19
 */
public class ConvertUtil {
    public static Object convert(String value) {
        if (value == null) {
            return null;
        }

        // 处理带引号的字符串
        if (isQuotedString(value)) {
            return value.substring(1, value.length() - 1);
        }

        // 尝试转换为boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Convert.toBool(value);
        }

        // 尝试转换为整数
        if (isInteger(value)) {
            return Convert.toInt(value);
        }

        // 尝试转换为浮点数
        if (isDouble(value)) {
            return Convert.toDouble(value);
        }

        // 默认返回字符串
        return value;
    }

    /**
     * 判断是否为带引号的字符串
     */
    private static boolean isQuotedString(String value) {
        return value != null && value.length() >= 2 &&
                ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")));
    }

    /**
     * 判断是否为整数
     */
    private static boolean isInteger(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断是否为浮点数
     */
    private static boolean isDouble(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
