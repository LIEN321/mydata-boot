package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import cn.hutool.core.collection.CollUtil;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataFilter;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.List;
import java.util.Map;

/**
 * 从数据仓库查询业务数据
 *
 * @author LIEN
 * @since 2024/12/6
 */
public class QueryDataFromWarehouse extends TaskExecutor {
    private final IDataFieldService dataFieldService = SpringUtil.getBean(IDataFieldService.class);
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
    private final IBizDataService bizDataService = SpringUtil.getBean(IBizDataService.class);

    public QueryDataFromWarehouse(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        Long dataId = pipelineTask.getDataId();
        if (ObjectUtil.isNull(dataId)) {
            error("查询失败：未选择数据标准");
            throw new RuntimeException("查询失败：未选择数据标准");
        }

        // 输出参数
        Map<String, String> outputMap = getOutputMap();
        String bizDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("查询失败：无效的输出设置，未配置查询结果的变量名");
            throw new RuntimeException("查询失败：无效的输出设置，未配置查询结果的变量名");
        }

        Data data = MyDataCache.getData(dataId);

        Project project = MyDataCache.getProject(pipelineTask.getProjectId());

        // 数据仓库名称
        String warehouseName = MyDataUtil.getBizDbCode(pipelineTask.getTenantId(), project.getProjectCode());
        // 标准数据的编号
        String dataCode = data.getDataCode();

        // 查询条件
        List<Map<String, Object>> dataFilterConfig = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("DATA_FILTER");
        List<BizDataFilter> dataFilters = convertBizDataFilter(dataFilterConfig);

        log("开始查询数据：{}", data.getDataName());
        log("查询条件：{}", CollectionUtil.emptyIfNull(dataFilters));

        // 查询业务数据
        List<Map<String, Object>> bizDataList = bizDataDAO.list(warehouseName, dataCode, dataFilters);

        log("查询结果：共 {} 条", bizDataList.size());

        // 输出参数
        jobContextData.put(bizDataKey, bizDataList);

        jobContextData.put(MyDataConstant.JOB_DATA_KEY_DATA_ID, dataId);
        jobContextData.put(MyDataConstant.JOB_DATA_KEY_DATA_CODE, dataCode);
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
}
