package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.data.BizDataDAO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IBizDataService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.Map;

/**
 * 从数仓中清空指定业务数据
 *
 * @author LIEN
 * @since 2024/11/25
 */
public class TruncateData extends TaskExecutor {
    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
    private final IBizDataService bizDataService = SpringUtil.getBean(IBizDataService.class);

    public TruncateData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();
        Long dataId = pipelineTask.getDataId();

        // 获取数据标准
        Data data = MyDataCache.getData(dataId);
        AssertUtil.notNull(data);

        // 数据仓库名称
        String warehouseName = getWarehouseName();

        bizDataDAO.drop(warehouseName, data.getDataCode());
        bizDataService.updateDataCount(dataId);
        log("清空数据成功");
    }
}
