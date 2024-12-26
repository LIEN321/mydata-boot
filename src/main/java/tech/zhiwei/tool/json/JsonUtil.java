package tech.zhiwei.tool.json;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import tech.zhiwei.tool.lang.StringUtil;

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

    /**
     * 判断json对象是否为空，包括：null、空字符串、{}、[]
     *
     * @param json json对象
     * @return 是否为空json
     */
    public static boolean isEmpty(JSON json) {
        if (json == null) {
            return true;
        }

        if (json instanceof JSONObject && ((JSONObject) json).isEmpty()) {
            return true;
        }

        return json instanceof JSONArray && ((JSONArray) json).isEmpty();
    }

    /**
     * 判断字符串是否为空的json，包括：null、空字符串、{}、[]
     *
     * @param jsonStr json字符串
     * @return 是否为空json
     */
    public static boolean isEmpty(String jsonStr) {
        if (StringUtil.isEmpty(jsonStr)) {
            return true;
        }

        JSON json = JSONUtil.parse(jsonStr);

        return isEmpty(json);
    }

    /**
     * 判断json对象是否不为空
     *
     * @param json json对象
     * @return 是否不为空json
     */
    public static boolean isNotEmpty(JSON json) {
        return !isEmpty(json);
    }

    /**
     * 判断json对象是否不为空
     *
     * @param jsonStr json字符串
     * @return 是否不为空json
     */
    public static boolean isNotEmpty(String jsonStr) {
        return !isEmpty(jsonStr);
    }
}
