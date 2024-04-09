package org.springblade.modules.mydata.job.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.data.BizDataFilter;
import org.springblade.modules.mydata.job.bean.TaskInfo;
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
     * @param taskInfo
     */
    public void doFilter(TaskInfo taskInfo) {
        Assert.notNull(taskInfo);

        List<Map> dataList = taskInfo.getProduceDataList();
        List<BizDataFilter> dataFilters = taskInfo.getDataFilters();

        if (CollUtil.isEmpty(dataList) || CollUtil.isEmpty(dataFilters)) {
            return;
        }

        // 过滤后的有效数据
        List<Map> validDataList = ListUtil.toList();
        // 过滤被拦截的无效数据
        List<Map> filteredDataList = ListUtil.toList();
        // 遍历数据，并进行过滤
        dataList.forEach(data -> {

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

            // 当 未被过滤，则添加到过滤结果
            if (isCorrect) {
                validDataList.add(data);
            } else {
                filteredDataList.add(data);
            }
        });

        taskInfo.setProduceDataList(validDataList);
        taskInfo.getFilteredDataList().addAll(filteredDataList);
    }

    /**
     * 解析过滤条件中的 自定义字符串
     */
    public List<BizDataFilter> parseFilterValue(TaskInfo taskInfo) {
        List<BizDataFilter> filters = taskInfo.getDataFilters();
        if (CollUtil.isEmpty(filters)) {
            return filters;
        }

        filters.forEach(filter -> {
            Object value = filter.getValue();
            // 任务的最后成功时间，若没有成功过 则复用任务开始时间
            if (MdConstant.DATA_VALUE_TASK_LAST_SUCCESS_TIME.equals(value)) {
                filter.setValue(taskInfo.getLastSuccessTime());
            }
        });

        return filters;
    }

    public void sendNoticeEmail(TaskInfo taskInfo) {
        List<Map> filteredDataList = taskInfo.getFilteredDataList();
        // 若没有被过滤的无效数据，则结束
        if (CollUtil.isEmpty(filteredDataList)) {
            return;
        }
    }
}
