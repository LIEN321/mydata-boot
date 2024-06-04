package org.springblade.modules.mydata.job.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.modules.mydata.data.BizDataFilter;
import org.springblade.modules.mydata.job.bean.TaskJob;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 任务数据过滤器
 *
 * @author LIEN
 * @since 2021/2/13
 */
@Component
public class JobDataFilterService {
    /**
     * 将数据库中的过滤条件 转为封装类结构
     */
    public List<BizDataFilter> convertBizDataFilter(List<Map<String, Object>> dataFilterList) {
        if (CollUtil.isEmpty(dataFilterList)) {
            return null;
        }

        List<BizDataFilter> bizDataFilters = CollUtil.newArrayList();
        for (Map<String, Object> map : dataFilterList) {
            BizDataFilter bizDataFilter = new BizDataFilter();
            bizDataFilter.setKey(map.get(MdConstant.PARAM_KEY).toString());
            bizDataFilter.setOp(map.get(MdConstant.PARAM_OP).toString());
            bizDataFilter.setValue(map.get(MdConstant.PARAM_VALUE));
            bizDataFilter.setType(map.get(MdConstant.PARAM_TYPE));
            bizDataFilters.add(bizDataFilter);
        }

        return bizDataFilters;
    }

    /**
     * 通过 task里的dataFitler 对datas进行过滤
     *
     * @param taskJob
     */
    public void doFilter(TaskJob taskJob) {
        Assert.notNull(taskJob);

        List<Map<String, Object>> dataList = taskJob.getProduceDataList();
        List<BizDataFilter> dataFilters = taskJob.getDataFilters();

        if (CollUtil.isEmpty(dataList) || CollUtil.isEmpty(dataFilters)) {
            return;
        }

        // 数据的标识字段编号，用于检测 标识字段值 是否有效
        String dataIdCode = taskJob.getIdFieldCode();
        List<String> dataIdCodes = StrUtil.split(dataIdCode, StrPool.COMMA);

        Map<String, String> fieldTypeMapping = taskJob.getFieldTypeMapping();

        // 过滤后的有效数据
        List<Map<String, Object>> validDataList = ListUtil.toList();
        // 过滤被拦截的无效数据
        List<Map<String, Object>> filteredDataList = ListUtil.toList();
        // 遍历数据，并进行过滤
        dataList.forEach(data -> {

            // 当数据未被过滤，则添加到过滤结果
            if (checkIdValue(data, dataIdCodes) && filterDataValues(data, fieldTypeMapping, dataFilters)) {
                validDataList.add(data);
            } else {
                filteredDataList.add(data);
            }
        });

        taskJob.setProduceDataList(validDataList);
        taskJob.getFilteredDataList().addAll(filteredDataList);
    }

    /**
     * 解析过滤条件中的 自定义字符串
     */
    public List<BizDataFilter> parseFilterValue(TaskJob taskJob) {
        List<BizDataFilter> filters = taskJob.getDataFilters();
        if (CollUtil.isEmpty(filters)) {
            return filters;
        }

        filters.forEach(filter -> {
            Object value = filter.getValue();
            // 任务的最后成功时间，若没有成功过 则复用任务开始时间
            if (MdConstant.DATA_VALUE_TASK_LAST_SUCCESS_TIME.equals(value)) {
                filter.setValue(taskJob.getLastSuccessTime());
            }
        });

        return filters;
    }

    private boolean checkIdValue(Map<String, Object> data, List<String> idCodes) {
        // 检测 标识字段值 是否有效
        for (String idCode : idCodes) {
            Object idFieldValue = data.get(idCode);
            if (ObjectUtil.isNull(idFieldValue)) {
                return false;
            }
        }
        return true;
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
            filterValue = MdUtil.convertDataType(filterValue, fieldTypeMapping.get(key));

            // 判断业务数据值 和 过滤数据值 都可对比，否则过滤条件无效
//                if (!(dataValue instanceof Comparable && filterValue instanceof Comparable)) {
//                    break;
//                }

            Comparable cDataValue = (Comparable) dataValue;
            Comparable cFilterValue = (Comparable) filterValue;
            // 根据op类型，过滤数据
            switch (op) {
                case MdConstant.DATA_NOT_NULL:
                    // not null
                    isCorrect = ObjectUtil.isNotNull(dataValue);
                    break;
                case MdConstant.DATA_NOT_EMPTY:
                    // not empty
                    isCorrect = ObjectUtil.isNotEmpty(dataValue);
                    break;
                case MdConstant.DATA_OP_EQ:
                    // 等于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) == 0);
                    break;
                case MdConstant.DATA_OP_NE:
                    // 不等于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) != 0);
                    break;
                case MdConstant.DATA_OP_GT:
                    // 大于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) > 0);
                    break;
                case MdConstant.DATA_OP_GTE:
                    // 大于等于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) >= 0);
                    break;
                case MdConstant.DATA_OP_LT:
                    // 小于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) < 0);
                    break;
                case MdConstant.DATA_OP_LTE:
                    // 小于等于
                    isCorrect = (ObjectUtil.compare(cDataValue, cFilterValue) <= 0);
                    break;

                default:
                    throw new RuntimeException("JobDataFilter: 不支持的过滤操作");
            }
        }

        return isCorrect;
    }
}