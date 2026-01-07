package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobVarService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 从数据仓库查询业务数据
 *
 * @author LIEN
 * @since 2024/12/6
 */
public class QueryDataFromWarehouse extends TaskExecutor {
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);

    // public QueryDataFromWarehouse(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        PipelineTask pipelineTask = getPipelineTask();

        // 输入参数
        Map<String, String> inputMap = getInputMap();
        // 获业务数据的key
        String paramBizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_PARAM_DATA);
        PipelineBizData paramBizData = null;
        if (StringUtil.isNotEmpty(paramBizDataKey)) {
            // 上下文业务数据
            paramBizData = (PipelineBizData) pipelineContext.get(paramBizDataKey);
        }

        Long dataId = pipelineTask.getDataId();
        if (ObjectUtil.isNull(dataId)) {
//            error("查询失败：未选择数据标准");
            throw new RuntimeException("查询失败：未选择数据标准");
        }

        // 输出参数
        Map<String, String> outputMap = getOutputMap();
        String outputBizDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(outputBizDataKey)) {
//            error("查询失败：无效的输出设置，未配置查询结果的变量名");
            throw new RuntimeException("查询失败：无效的输出设置，未配置查询结果的变量名");
        }

        // 界面配置的查询条件
        List<Map<String, Object>> dataFilterConfig = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("DATA_FILTER");
        List<BizDataFilter> dataFilters = MyDataUtil.convertBizDataFilter(dataFilterConfig);
        dataFilters = CollectionUtil.emptyIfNull(dataFilters);

        // 自定义输入的查询条件
        String condition = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_CONDITION);

        // 处理查询条件中的上下文变量
        if (paramBizData != null) {
            if (CollectionUtil.isEmpty(paramBizData.getBizData())) {
                info("没有业务数据可作为参数，结束执行");
                return;
            } else {
                Map<String, Object> bizDataMap = MapUtil.newHashMap();
                // 字段编号-字段类型
                Map<String, String> fieldTypeMapping = paramBizData.getDataFields().stream().collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                bizDataMap.putAll(paramBizData.getBizData().get(0));

                dataFilters.forEach(filter -> {
                    if (JobVarService.isFieldExp(filter.getValue().toString())) {
                        String filterValue = JobVarService.processDataFieldVar(filter.getValue().toString(), bizDataMap, fieldTypeMapping);
                        filter.setValue(filterValue);
                    }
                });
            }
        }

        // 标准数据
        Data data = MyDataCache.getData(dataId);

        // 数据仓库名称
        String warehouseName = getWarehouseName();
        // 标准数据的编号
        String dataCode = data.getDataCode();

        info("开始查询数据：{}", data.getDataName());

        info("查询条件：{}", dataFilters);
        // 查询业务数据
        List<Map<String, Object>> bizDataList = bizDataDAO.list(warehouseName, dataCode, dataFilters, condition);
        MyDataUtil.processBizData(bizDataList);

        info("查询结果：共 {} 条", bizDataList.size());

        // 字段列表
        List<DataField> dataFields = getDataFields(dataId);
        // 输出参数
//        pipelineContext.put(bizDataKey, bizDataList);
        PipelineBizData outputBizData = new PipelineBizData(dataId, dataCode, dataFields, bizDataList);
        pipelineContext.put(outputBizDataKey, outputBizData);
    }
}
