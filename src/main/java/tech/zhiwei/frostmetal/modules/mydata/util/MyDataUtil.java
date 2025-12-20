package tech.zhiwei.frostmetal.modules.mydata.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineCondition;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.io.FileUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.util.EnumUtil;
import tech.zhiwei.tool.util.NumberUtil;
import tech.zhiwei.tool.util.SystemUtil;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * mydata 工具类
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class MyDataUtil {

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
        return MyDataConstant.API_TYPE_DATA_PRODUCER == opType || MyDataConstant.API_TYPE_DATA_CONSUMER == opType;
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
        return url.startsWith(MyDataConstant.HTTP) || url.startsWith(MyDataConstant.HTTPS);
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
        return EnumUtil.contains(MyDataConstant.HttpMethod.class, method.toUpperCase());
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

        return EnumUtil.contains(MyDataConstant.ApiDataType.class, apiDataType.toUpperCase());
    }

    /**
     * 将接口参数的k-v格式 转为 表格的k列和v列
     *
     * @param map Map
     * @return map List<Map<String, Object>>
     */
    public static List<Map<String, Object>> switchMapToList(Map<String, String> map) {
        List<Map<String, Object>> maps = CollectionUtil.newArrayList();
        if (CollectionUtil.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                Map<String, Object> item = MapUtil.newHashMap();
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
     * @param list List<Map<String, Object>>
     * @return Map
     */
    public static LinkedHashMap<String, String> parseToKvMap(List<Map<String, String>> list) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                map.put(item.get("k"), item.get("v"));
            });
        }
        return map;
    }

    /**
     * 将表格的k列和v列 转为 接口参数的k:v格式
     *
     * @param list List<Map<String, Object>>
     * @return Map
     */
    public static LinkedHashMap<String, String> parseToKvMapObj(List<Map<String, Object>> list) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                map.put((String) item.get("k"), StringUtil.toStringOrEmpty(item.get("v")));
            });
        }
        return map;
    }

    public static String getBizDbCode(String tenantId, String projectCode) {
        return tenantId + "_" + projectCode;
    }

    public static String getBizHistoryCollection(String dataCode) {
        return dataCode + MyDataConstant.DATA_COLUMN_HISTORY;
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
            String stringValue = StrUtil.toString(value);
            if (StrUtil.isEmpty(stringValue)) {
                return value;
            }
            convertValue = switch (targetType) {
                case MyDataConstant.DATA_TYPE_INT -> NumberUtil.parseInt(StrUtil.toString(value));
                case MyDataConstant.DATA_TYPE_STRING -> StrUtil.toString(value);
                case MyDataConstant.DATA_TYPE_DATE -> DateUtil.parse(StrUtil.toString(value));
                case MyDataConstant.DATA_TYPE_NUMBER -> NumberUtil.parseNumber(StrUtil.toString(value));
                default -> convertValue;
            };
        }

        return convertValue;
    }

    public static String formatData(Object value, String targetType) {
        if (ObjUtil.isNotNull(value) && StrUtil.isNotEmpty(targetType)) {
            switch (targetType) {
                case MyDataConstant.DATA_TYPE_INT:
                case MyDataConstant.DATA_TYPE_STRING:
                case MyDataConstant.DATA_TYPE_NUMBER:
                    return StrUtil.toString(value);
                case MyDataConstant.DATA_TYPE_DATE:
                    Date date = DateUtil.parse(StrUtil.toString(value));
                    return DateUtil.formatDateTime(date);
            }
        }

        return StrUtil.toString(value);
    }

    public static String defaultValue(String targetType) {
        return switch (targetType) {
            case MyDataConstant.DATA_TYPE_INT, MyDataConstant.DATA_TYPE_NUMBER -> "0";
//            case MdConstant.DATA_TYPE_STRING:
            case MyDataConstant.DATA_TYPE_DATE -> DateUtil.formatDateTime(new Date());
            default -> "";
        };
    }

    /**
     * 保存项目中 导出的excel文件
     *
     * @param tenantId    租户id
     * @param projectCode 项目编号
     * @param excelFile   excel文件
     * @return 保存的excel文件
     */
    public static File saveExcelFile(String tenantId, String projectCode, File excelFile) {
        File excelDir = new File(SystemUtil.getUserDir()
                + FileUtil.FILE_SEPARATOR + "files"
                + FileUtil.FILE_SEPARATOR + tenantId
                + FileUtil.FILE_SEPARATOR + projectCode
        );
        if (!excelDir.exists()) {
            excelDir.mkdirs();
        }
        File targetFile = new File(excelDir, excelFile.getName());
        FileUtil.copy(excelFile, targetFile, true);
        return targetFile;
    }

    /**
     * 处理业务数据中 特定类型数据的格式
     * 清除系统字段
     *
     * @param bizDataList 业务数据集合
     */
    public static void processBizData(List<Map<String, Object>> bizDataList) {
        if (CollectionUtil.isEmpty(bizDataList)) {
            return;
        }

        bizDataList.forEach(bizData -> {
            if (MapUtil.isEmpty(bizData)) {
                return;
            }

            // bizData.remove(MyDataConstant.MONGODB_OBJECT_ID);
            // bizData.remove(MyDataConstant.DATA_COLUMN_DATA_ID);
            // bizData.remove(MyDataConstant.DATA_COLUMN_UPDATE_TIME);

            bizData.forEach((k, v) -> {
                if (v instanceof Date) {
                    bizData.put(k, DateUtil.formatDateTime((Date) v));
                }
            });
        });
    }

    /**
     * 转换过滤条件，Map -> BizDataFilter
     *
     * @param dataFilterList 用户配置的查询条件
     * @return BizDataFilter集合
     */
    public static List<BizDataFilter> convertBizDataFilter(List<Map<String, Object>> dataFilterList) {
        if (CollUtil.isEmpty(dataFilterList)) {
            return null;
        }

        List<BizDataFilter> bizDataFilters = CollUtil.newArrayList();
        for (Map<String, Object> map : dataFilterList) {
            BizDataFilter bizDataFilter = new BizDataFilter();
            bizDataFilter.setKey((String) map.get("k"));
            bizDataFilter.setOp((String) map.get("op"));
            bizDataFilter.setValue(map.get("v"));
            bizDataFilter.setType(map.get("t"));
            bizDataFilters.add(bizDataFilter);
        }

        return bizDataFilters;
    }

    /**
     * 转换流水线条件，Map -> PipelineCondition
     *
     * @param mapList 流水线条件
     * @return PipelineCondition集合
     */
    public static List<PipelineCondition> convertPipelineCondition(List<Map<String, Object>> mapList) {
        if (CollUtil.isEmpty(mapList)) {
            return null;
        }

        List<PipelineCondition> pipelineConditions = CollUtil.newArrayList();
        for (Map<String, Object> map : mapList) {
            PipelineCondition pipelineCondition = new PipelineCondition();
            pipelineCondition.setKey((String) map.get("k"));
            pipelineCondition.setOp((String) map.get("op"));
            pipelineCondition.setValue(map.get("v"));
            pipelineConditions.add(pipelineCondition);
        }

        return pipelineConditions;
    }
}