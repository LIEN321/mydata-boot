package tech.zhiwei.tool.json;

import cn.hutool.json.JSONUtil;

import java.util.Map;

/**
 * json工具类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class JsonUtil extends JSONUtil {
    /**
     * 对象转为json字符串
     *
     * @param obj 被转换对象
     * @return json字符串
     * @see JSONUtil#toJsonStr(Object)
     */
    public static String toJsonString(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    /**
     * JSON字符串转Map
     *
     * @param obj json字符串
     * @return Map
     */
    public static Map<String, Object> parseToMap(Object obj) {
        return JSONUtil.parseObj(obj);
    }
}
