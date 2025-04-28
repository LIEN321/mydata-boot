package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import cn.hutool.core.date.DateUnit;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineLogService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
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
    private final IPipelineLogService pipelineLogService = SpringUtil.getBean(IPipelineLogService.class);

    private volatile boolean interrupted = false;

    // Job执行过程中的变量
    private final Map<String, Object> jobContextData = new HashMap<>();

    public void execute(Map<String, Object> paramMap, Long pipelineId, Integer triggerType) throws JobExecutionException {
        // 流水线参数
        Map<String, Object> triggerParam = ObjectUtil.cloneByStream(paramMap);
        triggerParam.remove(MyDataConstant.JOB_DATA_KEY_TRIGGER_TYPE);
        triggerParam.remove(SysConstant.TENANT_ID);
        triggerParam.remove(MyDataConstant.JOB_DATA_KEY_PIPELINE_ID);
        // 流水线参数存入流程全局变量
        jobContextData.putAll(triggerParam);

        // 开始时间
        Date historyStartTime = new Date();

        log.info("PipelineJob execute");

        // 查询流水线记录
        Pipeline pipeline = pipelineService.getById(pipelineId);
        if (pipeline == null) {
            throw new JobExecutionException(StringUtil.format("执行失败：流水线不存在，id={}！", pipelineId));
        }

        // 创建流水线的执行记录
        PipelineHistoryDTO pipelineHistoryDTO = new PipelineHistoryDTO();
        pipelineHistoryDTO.setPipelineId(pipelineId);
        pipelineHistoryDTO.setTriggerType(triggerType);
        pipelineHistoryDTO.setStartTime(historyStartTime);
        pipelineHistoryDTO.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
        pipelineHistoryDTO.setTenantId(pipeline.getTenantId());
        pipelineHistoryDTO.setTriggerParam(triggerParam);
        // 保存流水线的执行记录
        Long historyId = pipelineHistoryService.savePipelineHistory(pipelineHistoryDTO);

        // 更新流水线的最新执行记录id
        pipeline.setLatestHistoryId(historyId);
        pipelineService.updateById(pipeline);

        // 查询流水线任务列表
        List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);

        // 任务 与 日志 的id映射
        Map<Long, Long> taskLogIdMapping = MapUtil.newHashMap();
        if (CollectionUtil.isNotEmpty(tasks)) {
            // 创建流水线的执行日志
            tasks.forEach(task -> {
                PipelineLog pipelineLog = new PipelineLog();
                pipelineLog.setPipelineId(pipelineId);
                pipelineLog.setHistoryId(historyId);
                pipelineLog.setTaskType(task.getTaskType());
                pipelineLog.setTaskName(task.getTaskName());
                pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_READY);
                pipelineLogService.save(pipelineLog);

                taskLogIdMapping.put(task.getId(), pipelineLog.getId());
            });
        }

        // 待更新的流水线历史记录
        PipelineHistory pipelineHistory = new PipelineHistory();
        pipelineHistory.setId(historyId);

        try {
            if (CollectionUtil.isNotEmpty(tasks)) {
                for (PipelineTask pipelineTask : tasks) {
                    // 任务开始
                    Date taskStartTime = new Date();
                    PipelineLog pipelineLog = new PipelineLog();
                    pipelineLog.setId(taskLogIdMapping.get(pipelineTask.getId()));
                    // 更新任务日志的开始时间
                    pipelineLog.setStartTime(taskStartTime);

                    if (interrupted) {
                        // 停止执行流水线
                        // 更新执行记录为中止
                        pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
                        // 更新任务日志为中止
                        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
                        break;
                    }

                    try {
                        // 任务禁用状态
                        if (ObjectUtil.equals(pipelineTask.getStatus(), SysConstant.STATUS_DISABLED)) {
                            // 禁用的任务 状态为跳过
                            pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SKIP);
                            pipelineLog.setTaskLog("该任务已禁用，不执行。");
                            continue;
                        }

                        // 更新任务日志的执行状态
                        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
                        pipelineLogService.updateById(pipelineLog);

                        // 执行任务
                        TaskExecutor.create(pipelineTask, pipelineLog).execute(jobContextData);

                        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
                    } catch (Exception e) {
                        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
                        log.error(e.getMessage(), e);
                        // 抛出异常，结束流水线和后续任务
                        throw e;
                    } finally {
                        // 任务执行结束
                        // 更新任务日志的结束时间
                        Date taskEndTime = new Date();
                        pipelineLog.setEndTime(taskEndTime);
                        // 计算任务执行的耗时
                        pipelineLog.setExecutionTime(DateUtil.between(taskStartTime, taskEndTime, DateUnit.SECOND));
                        try {
                            // 更新任务日志
                            pipelineLogService.updateById(pipelineLog);
                        } catch (Exception e) {
                            // 更新任务日志失败，则停止任务
                            pipelineLogService.failLog(pipelineLog.getId());
                            log.error(e.getMessage(), e);
                            throw e;
                        }
                    }
                }
            }
            if (pipelineHistory.getExecutionStatus() == null) {
                pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
            }
        } catch (Exception e) {
            pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
            log.error(e.getMessage(), e);
        }

        // 结束时间
        Date historyEndTime = new Date();

        // 更新流水线结果
        pipelineHistory.setEndTime(historyEndTime);
        pipelineHistory.setExecutionTime(DateUtil.between(historyStartTime, historyEndTime, DateUnit.SECOND));
        pipelineHistoryService.updateById(pipelineHistory);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 流水线id
        Long pipelineId = context.getJobDetail().getJobDataMap().getLong(MyDataConstant.JOB_DATA_KEY_PIPELINE_ID);
        // 流水线出发类型
        Integer triggerType = context.getJobDetail().getJobDataMap().getInt(MyDataConstant.JOB_DATA_KEY_TRIGGER_TYPE);

        execute(context.getJobDetail().getJobDataMap(), pipelineId, triggerType);
    }

    @Override
    public void interrupt() {
        log.info("job interrupted");
        interrupted = true;
    }
}
