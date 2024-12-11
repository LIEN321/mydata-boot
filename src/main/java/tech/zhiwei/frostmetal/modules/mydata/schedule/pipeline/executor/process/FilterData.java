package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process;

import cn.hutool.core.collection.CollUtil;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 过滤数据
 *
 * @author LIEN
 * @since 2024/12/7
 */
public class FilterData extends TaskExecutor {

    public FilterData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        // 输入参数
        Map<String, String> inputMap = getInputMap();
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("执行失败：未配置待过滤的业务数据变量，无法获取业务数据");
            throw new IllegalArgumentException("执行失败：未配置待过滤的业务数据变量，无法获取业务数据");
        }

        // 获取上下文的业务数据
//        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(bizDataKey);
        PipelineBizData pipelineBizData = (PipelineBizData) jobContextData.get(bizDataKey);
        if (pipelineBizData == null) {
            error("执行失败：前置任务没有输出有效的业务数据");
            throw new IllegalArgumentException("执行失败：前置任务没有输出有效的业务数据");
        }
        List<Map<String, Object>> bizDataList = pipelineBizData.getBizData();
        if (CollectionUtil.isEmpty(bizDataList)) {
            log("待过滤的数据为空，结束执行");
            return;
        }

        // 过滤条件
        List<Map<String, Object>> dataFilterConfig = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("DATA_FILTER");
        List<BizDataFilter> dataFilters = convertBizDataFilter(dataFilterConfig);
        if (CollectionUtil.isEmpty(dataFilters)) {
            error("执行失败：未配置过滤条件，结束执行");
            throw new RuntimeException("执行失败：未配置过滤条件，结束执行");
        }

        Long dataId = pipelineBizData.getDataId();
        if (ObjectUtil.isNull(dataId)) {
            error("执行失败：前置任务未选择标准数据，结束执行");
            throw new RuntimeException("执行失败：前置任务未选择标准数据，结束执行");
        }

        // 输出参数
        Map<String, String> outputMap = getOutputMap();
        String filteredDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(filteredDataKey)) {
            error("执行失败：无效的输出设置，未配置过滤结果的变量名");
            throw new RuntimeException("执行失败：无效的输出设置，未配置过滤结果的变量名");
        }

        // 标准数据字段列表
        List<DataField> dataFields = getDataFields(dataId);

        // 字段编号-字段类型
        Map<String, String> fieldTypeMapping = dataFields.stream()
                .collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));

        log("过滤前，业务数据总数：{}", bizDataList.size());
        log("过滤条件：{}", dataFilters);

        log("过滤数据开始...");

        // 过滤后的有效数据
        List<Map<String, Object>> validDataList = CollectionUtil.toList();
        // 过滤被拦截的无效数据
        List<Map<String, Object>> blockedDataList = CollectionUtil.toList();
        // 遍历数据，并进行过滤
        bizDataList.forEach(bizData -> {

            // 当数据未被过滤，则添加到过滤结果
//            if (checkIdValue(data, dataIdCodes) && filterDataValues(data, fieldTypeMapping, dataFilters)) {
            if (filterDataValues(bizData, fieldTypeMapping, dataFilters)) {
                validDataList.add(bizData);
            } else {
                blockedDataList.add(bizData);
            }
        });

        log("过滤数据结束，有效的业务数据 {} 条，被过滤拦截了 {} 条", validDataList.size(), blockedDataList.size());

        // 输出参数
//        jobContextData.put(bizDataKey, validDataList);
        pipelineBizData.setBizData(validDataList);
        jobContextData.put(bizDataKey, pipelineBizData);
        String blockedDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_FILTER_BLOCKED_DATA);
        if (StringUtil.isNotEmpty(blockedDataKey)) {
            jobContextData.put(blockedDataKey, blockedDataList);
        }
    }

    private List<BizDataFilter> convertBizDataFilter(List<Map<String, Object>> dataFilterList) {
        if (CollUtil.isEmpty(dataFilterList)) {
            return null;
        }

        List<BizDataFilter> bizDataFilters = CollUtil.newArrayList();
        for (Map<String, Object> map : dataFilterList) {
            BizDataFilter bizDataFilter = new BizDataFilter();
            bizDataFilter.setKey(map.get("k").toString());
            bizDataFilter.setOp(map.get("op").toString());
            bizDataFilter.setValue(map.get("v"));
            bizDataFilter.setType(map.get("t"));
            bizDataFilters.add(bizDataFilter);
        }

        return bizDataFilters;
    }

    private boolean filterDataValues(Map<String, Object> data, Map<String, String> fieldTypeMapping, List<BizDataFilter> dataFilters) {
        boolean isCorrect = false;

        for (BizDataFilter filter : dataFilters) {
            String key = filter.getKey();
            Object filterValue = filter.getValue();
            String op = filter.getOp();

            // 当数据中 不包含 过滤的字段名，则执行下一项过滤
            if (!data.containsKey(key)) {
                continue;
            }

            // 当数据中 指定字段的值 无效，则过滤该数据
            Object dataValue = data.get(key);
            // 将条件值转换为字段的相同类型
            filterValue = MyDataUtil.convertDataType(filterValue, fieldTypeMapping.get(key));

            // 判断业务数据值 和 过滤数据值 都可对比，否则过滤条件无效
            if (!(dataValue instanceof Comparable && filterValue instanceof Comparable)) {
                throw new IllegalArgumentException(
                        StringUtil.format("过滤条件无效：{}字段的值{} 或过滤条件值{} 无法进行对比"
                                , key
                                , dataValue
                                , filterValue
                        )
                );
            }

            Comparable cDataValue = (Comparable) dataValue;
            Comparable cFilterValue = (Comparable) filterValue;
            // 根据op类型，过滤数据
            isCorrect = switch (op) {
                // not null
                case MyDataConstant.DATA_NOT_NULL -> ObjectUtil.isNotNull(dataValue);
                // not empty
                case MyDataConstant.DATA_NOT_EMPTY -> ObjectUtil.isNotEmpty(dataValue);
                // 等于
                case MyDataConstant.DATA_OP_EQ -> (ObjectUtil.compare(cDataValue, cFilterValue) == 0);
                // 不等于
                case MyDataConstant.DATA_OP_NE -> (ObjectUtil.compare(cDataValue, cFilterValue) != 0);
                // 大于
                case MyDataConstant.DATA_OP_GT -> (ObjectUtil.compare(cDataValue, cFilterValue) > 0);
                // 大于等于
                case MyDataConstant.DATA_OP_GTE -> (ObjectUtil.compare(cDataValue, cFilterValue) >= 0);
                // 小于
                case MyDataConstant.DATA_OP_LT -> (ObjectUtil.compare(cDataValue, cFilterValue) < 0);
                // 小于等于
                case MyDataConstant.DATA_OP_LTE -> (ObjectUtil.compare(cDataValue, cFilterValue) <= 0);

                default -> throw new IllegalArgumentException(
                        StringUtil.format("过滤条件无效: 不支持的过滤操作 {}", op)
                );
            };
        }

        return isCorrect;
    }
}
