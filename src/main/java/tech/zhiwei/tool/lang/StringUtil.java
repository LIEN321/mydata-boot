package tech.zhiwei.tool.lang;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.text.StringSubstitutor;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class StringUtil extends StrUtil {
    /**
     * 以默认的逗号 将集合对象转换为字符串
     *
     * @param coll 集合
     * @return 拼接后的字符串
     */
    public static String join(Collection<?> coll) {
        if (coll == null) {
            return null;
        }
        return join(StringPool.COMMA, coll.toArray());
    }

    /**
     * 以默认的逗号 分割字符串
     *
     * @param string 被分割的字符串
     * @return 字符串集合
     */
    public static Long[] splitToLong(String string) {
//        return splitToLong(string, StringPool.COMMA);
        return Convert.convert(Long[].class, splitTrim(string, StringPool.COMMA));
    }

    /**
     * 将${}格式的 ${key} 替换为 value
     *
     * @param source 原始字符串
     * @param key    ${key}表达式中的key
     * @param value  要替换的值
     * @return 替换后的字符串
     */
    public static String substitute(String source, String key, String value) {
        if (isBlank(source) || isBlank(key) || value == null) {
            return source;
        }

        Map<String, String> map = MapUtil.newHashMap();
        map.put(key, value);

        StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
        return stringSubstitutor.replace(source);
    }
}
