package org.springblade.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.springblade.common.constant.MdConstant;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * mydata 工具类
 *
 * @author LIEN
 * @since 2022/7/9
 */
public class MdUtil {

    // 用户自定义变量 ${} 的正则表达式
    private static final String USER_VAR_PATTERN = "\\$\\{([^}]*)\\}";

    // 系统内置变量 {$} 的正则表达式
    private static final String SYS_VAR_PATTERN = "\\{\\$([^}]*)\\}";

    /**
     * 校验 数据操作类型 是否有效
     *
     * @param opType 数据类型
     * @return true-有效，false-无效
     */
    public static boolean isValidOpType(Integer opType) {
        if (opType == null) {
            return false;
        }
        return MdConstant.DATA_PRODUCER == opType || MdConstant.DATA_CONSUMER == opType;
    }

    /**
     * 校验 请求方法 是否有效
     *
     * @param url 请求方法
     * @return 校验结果
     */
    public static boolean isValidHttpProtocol(String url) {
        if (StrUtil.isEmpty(url)) {
            return false;
        }
        return url.startsWith(MdConstant.HTTP) || url.startsWith(MdConstant.HTTPS);
    }

    /**
     * 校验 请求方法 是否有效
     *
     * @param method 请求方法
     * @return 校验结果
     */
    public static boolean isValidHttpMethod(String method) {
        if (StrUtil.isEmpty(method)) {
            return false;
        }
        return EnumUtil.contains(MdConstant.HttpMethod.class, method.toUpperCase());
    }

    /**
     * 校验 数据类型 是否有效
     *
     * @param apiDataType 数据类型
     * @return 校验结果
     */
    public static boolean isValidDataType(String apiDataType) {
        if (StrUtil.isEmpty(apiDataType)) {
            return false;
        }

        return EnumUtil.contains(MdConstant.ApiDataType.class, apiDataType.toUpperCase());
    }

    /**
     * 将接口参数的k-v格式 转为 表格的k列和v列
     *
     * @param map Map
     * @return List<Map>
     */
    public static List<Map<String, String>> switchMapToList(Map<String, String> map) {
        List<Map<String, String>> maps = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                Map<String, String> item = MapUtil.newHashMap();
                item.put("k", k);
                item.put("v", v);
                maps.add(item);
            });
        }
        return maps;
    }

    /**
     * 将表格的k列和v列 转为 接口参数的k-v格式
     *
     * @param list List<Map>
     * @return Map
     */
    public static LinkedHashMap<String, String> parseToKvMap(List<Map<String, String>> list) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                map.put(item.get("k"), item.get("v"));
            });
        }
        return map;
    }

    /**
     * 将表格的k列和v列 转为 接口参数的k-v格式
     *
     * @param list List<Map>
     * @return Map
     */
    public static LinkedHashMap<String, Object> parseToKvMapObj(List<Map<String, Object>> list) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                map.put((String) item.get("k"), item.get("v"));
            });
        }
        return map;
    }

    /**
     * 从字符串中 解析所有用户自定义表达式中的变量名
     *
     * @param string 字符串
     * @return 变量名列表
     */
    public static List<String> parseUserVarNames(String string) {
        return parseVarNames(string, USER_VAR_PATTERN);
    }

    /**
     * 从字符串中 解析所有系统内置变量表达式中的变量名
     *
     * @param string 字符串
     * @return 变量名列表
     */
    public static List<String> parseSysVarNames(String string) {
        return parseVarNames(string, SYS_VAR_PATTERN);
    }

    /**
     * 从字符串中 解析指定表达式中的变量名
     *
     * @param string  字符串
     * @param pattern 表达式
     * @return 变量名列表
     */
    public static List<String> parseVarNames(String string, String pattern) {
        if (StrUtil.isEmpty(string)) {
            return CollUtil.newArrayList();
        }

        List<String> varNames = ReUtil.findAll(pattern, string, 0);
        if (CollUtil.isNotEmpty(varNames)) {
            ListIterator<String> iterator = varNames.listIterator();
            while (iterator.hasNext()) {
                String varName = iterator.next();
                varName = getKey(varName);
                iterator.set(varName);
            }
        }
        return varNames;
    }

    /**
     * 从多个字符串中 解析所有${}表达式中的变量名
     *
     * @param strings 字符串集合
     * @return 变量名列表
     */
    public static List<String> parseUserVarNames(Collection<?> strings) {
        List<String> list = CollUtil.newArrayList();
        if (CollUtil.isNotEmpty(strings)) {
            for (Object string : strings) {
                list.addAll(parseUserVarNames(string.toString()));
            }
        }
        return list;
    }

    public static String getBizDbCode(String tenantId, Long projectId, Long envId) {
        return tenantId + ":" + projectId + ":" + envId;
    }

    private static String getKey(String g) {
        return g.substring(2, g.length() - 1);
    }
}
