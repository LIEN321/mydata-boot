package org.springblade.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import org.springblade.common.constant.MdConstant;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * mydata 工具类
 *
 * @author LIEN
 * @since 2022/7/9
 */
public class MdUtil {

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

    public static String getBizDbCode(String tenantId, Long projectId, Long envId) {
        return tenantId + ":" + projectId + ":" + envId;
    }

    /**
     * 根据字段类型配置，将接口数据 转为指定类型
     *
     * @param value      接口数据
     * @param targetType 转换目标类型
     * @return 转换后的数据
     */
    public static Object convertDataType(Object value, String targetType) {
        Object convertValue = value;
        if (ObjUtil.isNotNull(value) && StrUtil.isNotEmpty(targetType)) {
            switch (targetType) {
                case MdConstant.DATA_TYPE_INT:
                    convertValue = NumberUtil.parseInt(StrUtil.toString(value));
                    break;
                case MdConstant.DATA_TYPE_STRING:
                    convertValue = StrUtil.toString(value);
                    break;
                case MdConstant.DATA_TYPE_DATE:
                    convertValue = DateUtil.parse(StrUtil.toString(value));
                    break;
                case MdConstant.DATA_TYPE_NUMBER:
                    convertValue = NumberUtil.parseNumber(StrUtil.toString(value));
                    break;
            }
        }

        return convertValue;
    }

    public static String formatData(Object value, String targetType) {
        if (ObjUtil.isNotNull(value) && StrUtil.isNotEmpty(targetType)) {
            switch (targetType) {
                case MdConstant.DATA_TYPE_INT:
                case MdConstant.DATA_TYPE_STRING:
                case MdConstant.DATA_TYPE_NUMBER:
                    return StrUtil.toString(value);
                case MdConstant.DATA_TYPE_DATE:
                    Date date = DateUtil.parse(StrUtil.toString(value));
                    return DateUtil.formatDateTime(date);
            }
        }

        return StrUtil.toString(value);
    }
}