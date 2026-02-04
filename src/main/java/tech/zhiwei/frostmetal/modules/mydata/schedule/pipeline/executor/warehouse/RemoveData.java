package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.spring.SpringUtil;

/**
 * 从数仓中清空指定业务数据
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class RemoveData extends TaskExecutor {
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
    private final IBizDataService bizDataService = SpringUtil.getBean(IBizDataService.class);

    // public RemoveData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        PipelineTask pipelineTask = getPipelineTask();
        Long dataId = pipelineTask.getDataId();

        // 获取数据标准
        Data data = MyDataCache.getData(dataId);
        AssertUtil.notNull(data);

        // 数据仓库名称
        String warehouseName = getWarehouseName();

        // 自定义输入的查询条件
        String condition = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_CONDITION);
        info("删除条件：{}", StringUtil.isEmpty(condition) ? "无" : condition);
        long count = bizDataDAO.removeByCondition(warehouseName, data.getDataCode(), condition);
        info("删除数据 {} 条", count);

        // 更新业务数量
        bizDataService.updateDataCount(dataId);
    }
}
