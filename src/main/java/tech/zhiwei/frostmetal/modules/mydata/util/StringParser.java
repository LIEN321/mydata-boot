package tech.zhiwei.frostmetal.modules.mydata.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 字符串 自动转为合适类型对象
 * 比如：0转为Integer，"0"转为String，true和false转为Boolean
 */
public class StringParser {
    public static Object autoParse(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }

        // 处理带引号的字符串（单引号或双引号）
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        // 布尔值检查
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Convert.convert(Boolean.class, value);
        }

        // 数字检查
        if (NumberUtil.isNumber(value)) {
            if (NumberUtil.isInteger(value)) {
                return Convert.convert(Integer.class, value);
            } else {
                return Convert.convert(Double.class, value);
            }
        }

        return value;
    }
}