package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import cn.hutool.core.date.DateUnit;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流水线Job
 *
 * @author LIEN
 * @since 2024/11/20
 */
@Slf4j
public class PipelineJob implements InterruptableJob {
    private final IPipelineService pipelineService = SpringUtil.getBean(IPipelineService.class);
    private final IPipelineTaskService pipelineTaskService = SpringUtil.getBean(IPipelineTaskService.class);
    private final IPipelineHistoryService pipelineHistoryService = SpringUtil.getBean(IPipelineHistoryService.class);

    private volatile boolean interrupted = false;

    // Job执行过程中的变量
    private Map<String, Object> jobContextData = new HashMap<String, Object>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 开始时间
        Date startTime = new Date();

        log.info("PipelineJob execute");
        // 获取流水线id
        Long pipelineId = context.getJobDetail().getJobDataMap().getLong(MdConstant.JOB_DATA_KEY_PIPELINE_ID);

        // 查询流水线记录
        Pipeline pipeline = pipelineService.getById(pipelineId);
        if (pipeline == null) {
            throw new JobExecutionException(StringUtil.format("Job执行失败：流水线不存在，id={}！", pipelineId));
        }

        // 创建流水线的执行记录
        Integer triggerType = context.getJobDetail().getJobDataMap().getInt(MdConstant.JOB_DATA_KEY_TRIGGER_TYPE);
        PipelineHistoryDTO pipelineHistoryDTO = new PipelineHistoryDTO();
        pipelineHistoryDTO.setPipelineId(pipelineId);
        pipelineHistoryDTO.setTriggerType(triggerType);
        pipelineHistoryDTO.setStartTime(startTime);
        pipelineHistoryDTO.setExecutionStatus(MdConstant.PIPELINE_HISTORY_STATUS_RUNNING);
        pipelineHistoryDTO.setTenantId(pipeline.getTenantId());
        Long historyId = pipelineHistoryService.savePipelineHistory(pipelineHistoryDTO);

        // 更新流水线的最新执行记录id
        pipeline.setLatestHistoryId(historyId);
        pipelineService.updateById(pipeline);

        // 查询流水线任务列表
        List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);

        // 待更新的流水线历史记录
        PipelineHistory pipelineHistory = new PipelineHistory();
        pipelineHistory.setId(historyId);

        try {
            if (CollectionUtil.isNotEmpty(tasks)) {
                for (PipelineTask task : tasks) {
                    if (interrupted) {
                        pipelineHistory.setExecutionStatus(MdConstant.PIPELINE_HISTORY_STATUS_STOPPED);
                        System.out.println("job break");
                        break;
                    }
                    // TODO 记录执行过程log
                    // 执行任务
                    TaskExecutor.getExecutor(task).execute(jobContextData);
                    log.info(jobContextData.toString());
                }
            }
            if (pipelineHistory.getExecutionStatus() == null) {
                pipelineHistory.setExecutionStatus(MdConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
            }
        } catch (Exception e) {
            pipelineHistory.setExecutionStatus(MdConstant.PIPELINE_HISTORY_STATUS_FAILED);
        }

        // 结束时间
        Date endTime = new Date();

        // 更新流水线结果
        pipelineHistory.setEndTime(endTime);
        pipelineHistory.setExecutionTime(DateUtil.between(startTime, endTime, DateUnit.SECOND));
        pipelineHistoryService.updateById(pipelineHistory);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.info("job interrupted");
        interrupted = true;
    }
}
