package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import cn.hutool.core.date.DateUnit;
import com.yomahub.liteflow.core.FlowExecutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.config.MydataConfiguration;
import tech.zhiwei.frostmetal.modules.mydata.constant.LiteFlowConstant;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.mail.MyDataMail;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineVar;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineLogService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineVarService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApp;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.StopPipelineException;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.ArrayUtil;

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
    /**
     * 流水线参数key：流水线内已认证的APP
     */
    public static final String PIPELINE_PARAM_KEY_AUTHED_APP = "PIPELINE_AUTHED_APP";

    private final IPipelineService pipelineService = SpringUtil.getBean(IPipelineService.class);
    private final IPipelineTaskService pipelineTaskService = SpringUtil.getBean(IPipelineTaskService.class);
    private final IPipelineHistoryService pipelineHistoryService = SpringUtil.getBean(IPipelineHistoryService.class);
    private final IPipelineLogService pipelineLogService = SpringUtil.getBean(IPipelineLogService.class);
    private final IUserService userService = SpringUtil.getBean(IUserService.class);
    private final PipelineScheduler pipelineScheduler = SpringUtil.getBean(PipelineScheduler.class);
    private final IPipelineVarService pipelineVarService = SpringUtil.getBean(IPipelineVarService.class);

    private volatile boolean interrupted = false;

    // Job执行过程中的变量
    private final Map<String, Object> jobContextData = new HashMap<>();

    private final MydataConfiguration mydataConfig = SpringUtil.getBean(MydataConfiguration.class);

    private FlowExecutor flowExecutor = SpringUtil.getBean(FlowExecutor.class);

    /**
     * 执行流水线
     *
     * @param triggerParam 触发流水线的入参
     * @param pipelineId   流水线id
     * @param triggerType  触发类型
     * @throws JobExecutionException 任务异常
     */
    public void execute(Map<String, Object> triggerParam, Long pipelineId, Integer triggerType) throws JobExecutionException {
        // 开始时间
        Date historyStartTime = new Date();

        log.info("PipelineJob execute");

        // 触发流水线的参数 存入流程全局变量
        jobContextData.putAll(triggerParam);

        // 流水线执行时 用于存储已认证应用的Map
        jobContextData.put(PIPELINE_PARAM_KEY_AUTHED_APP, new HashMap<Long, PipelineApp>());

        // 查询流水线记录
        Pipeline pipeline = pipelineService.getById(pipelineId);
        if (pipeline == null) {
            throw new JobExecutionException(StringUtil.format("执行失败：流水线不存在，id={}！", pipelineId));
        }

        // 流水线多次触发 进入等待队列，若连续失败 则会连续发送邮件
        // 检查流水线是否启用 定时或webhook（非手动触发执行）
        if (!MyDataConstant.JOB_TRIGGER_TYPE_MANUAL.equals(triggerType) && !(pipeline.getIsSchedule() || pipeline.getIsWebhook())) {
            throw new JobExecutionException(StringUtil.format("执行失败：流水线未启用定时或webhook"));
        }

        // 查询流水线变量
        List<PipelineVar> pipelineVars = pipelineVarService.listByPipeline(pipelineId);
        // 流水线变量有效时，存入流水线上下文
        if (CollectionUtil.isNotEmpty(pipelineVars)) {
            pipelineVars.forEach(var -> {
                jobContextData.put(var.getVarCode(), MyDataUtil.convertDataType(var.getVarValue(), var.getVarType()));
            });
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

        /*
        改用LiteFlow执行流水线的任务
        // 查询流水线任务列表
        // List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);

        // 任务 与 日志 的id映射
        // Map<Long, Long> taskLogIdMapping = MapUtil.newHashMap();
        // if (CollectionUtil.isNotEmpty(tasks)) {
        // 创建流水线的执行日志
        // tasks.forEach(task -> {
        //         PipelineLog pipelineLog = new PipelineLog();
        //         pipelineLog.setPipelineId(pipelineId);
        //         pipelineLog.setHistoryId(historyId);
        //         pipelineLog.setTaskType(task.getTaskType());
        //         pipelineLog.setTaskName(task.getTaskName());
        //         pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_READY);
        //         pipelineLogService.save(pipelineLog);
        //
        //         taskLogIdMapping.put(task.getId(), pipelineLog.getId());
        //     });
        // }
         */

        // 待更新的流水线历史记录
        PipelineHistory pipelineHistory = new PipelineHistory();
        pipelineHistory.setId(historyId);

        try {
            /*
            改用LiteFlow执行流水线的任务
            // if (CollectionUtil.isNotEmpty(tasks)) {
            //     for (PipelineTask pipelineTask : tasks) {
            //         // 任务开始
            //         // Date taskStartTime = new Date();
            //         // PipelineLog pipelineLog = new PipelineLog();
            //         // pipelineLog.setId(taskLogIdMapping.get(pipelineTask.getId()));
            //         // 更新任务日志的开始时间
            //         // pipelineLog.setStartTime(taskStartTime);
            //
            //         // if (interrupted) {
            //         //     // 停止执行流水线
            //         //     // 更新执行记录为中止
            //         //     pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
            //         //     // 更新任务日志为中止
            //         //     pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
            //         //     break;
            //         // }
            //
            //         // 执行任务
            //         // TaskExecutor taskExecutor = TaskExecutor.create(pipelineTask, pipelineLog);
            //
            //         // try {
            //         //     // 任务禁用状态
            //         //     // if (ObjectUtil.equals(pipelineTask.getStatus(), SysConstant.STATUS_DISABLED)) {
            //         //     //     // 禁用的任务 状态为跳过
            //         //     //     pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SKIP);
            //         //     //     pipelineLog.setTaskLog("该任务已禁用，不执行。");
            //         //     //     continue;
            //         //     // }
            //         //
            //         //     // 更新任务日志的执行状态
            //         //     // pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
            //         //     // pipelineLogService.updateById(pipelineLog);
            //         //
            //         //     // 执行任务
            //         //     taskExecutor.execute(jobContextData);
            //         //
            //         //     pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
            //         // } catch (StopPipelineException e) {
            //         //     // 停止流水线
            //         //     pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
            //         //     // 抛出异常，结束流水线和后续任务
            //         //     throw e;
            //         // } catch (Exception e) {
            //         //     // 异常，执行失败
            //         //     pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
            //         //     log.error(e.getMessage(), e);
            //         //
            //         //     // 判断preCondition，默认成功才继续
            //         //     Integer preCondition = ObjectUtil.defaultIfNull(pipelineTask.getPreCondition(), MyDataConstant.PIPELINE_TASK_PRE_CONDITION_SUCCESS);
            //         //     // 若为 总是继续，则不抛出异常，继续下个task
            //         //     if (MyDataConstant.PIPELINE_TASK_PRE_CONDITION_ALWAYS == preCondition) {
            //         //         taskExecutor.log("任务设置为 失败继续执行...");
            //         //         continue;
            //         //     }
            //         //
            //         //     // 抛出异常，结束流水线和后续任务
            //         //     throw e;
            //         // } finally {
            //             // 任务执行结束
            //             // 更新任务日志的结束时间
            //             // Date taskEndTime = new Date();
            //             // pipelineLog.setEndTime(taskEndTime);
            //             // 计算任务执行的耗时
            //             // pipelineLog.setExecutionTime(DateUtil.between(taskStartTime, taskEndTime, DateUnit.SECOND));
            //             // try {
            //             //     // 更新任务日志
            //             //     pipelineLogService.updateById(pipelineLog);
            //             // } catch (Exception e) {
            //             //     // 更新任务日志失败，则停止任务
            //             //     pipelineLogService.failLog(pipelineLog.getId());
            //             //     log.error(e.getMessage(), e);
            //             //     throw e;
            //             // }
            //         }
            //     }
            // }
             */

            // 获取流水线的el
            String el = pipeline.getLiteflowEl();
            if (StringUtil.isNotEmpty(el)) {
                Map<String, Object> params = MapUtil.newHashMap();
                params.put(LiteFlowConstant.BIND_KEY_HISTORY_ID, historyId);
                flowExecutor.execute2RespWithEL(el, params, null, jobContextData);
            } else {
                // 查询流水线任务列表
                List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);
                if (CollectionUtil.isNotEmpty(tasks)) {
                    for (PipelineTask task : tasks) {
                        // 执行任务
                        TaskExecutor taskExecutor = TaskExecutor.create(task);
                        taskExecutor.execute(historyId, task.getId(), jobContextData);
                    }
                }
            }

            if (pipelineHistory.getExecutionStatus() == null) {
                // 流水线执行成功
                pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
                // 连续失败次数置0
                pipeline.setConsecutiveFailures(0);
            }
        } catch (StopPipelineException e) {
            // 流水线执行终止
            pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
        } catch (Exception e) {
            // 流水线执行失败
            pipelineHistory.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
            // 连续失败次数+1
            int failures = ObjectUtil.defaultIfNull(pipeline.getConsecutiveFailures(), 0);
            failures++;
            pipeline.setConsecutiveFailures(failures);
            log.error(e.getMessage(), e);
        }

        // 结束时间
        Date historyEndTime = new Date();

        // 更新流水线历史记录
        pipelineHistory.setEndTime(historyEndTime);
        pipelineHistory.setExecutionTime(DateUtil.between(historyStartTime, historyEndTime, DateUnit.SECOND));
        pipelineHistoryService.updateById(pipelineHistory);

        // 流水线所属项目
        Project project = MyDataCache.getProject(pipeline.getProjectId());
        // 流水线创建者
        User creator = userService.getById(pipeline.getCreateUser());
        String creatorEmail = creator.getEmail();
        // 根据流水线的邮件通知配置，发送通知邮件
        Boolean isEmail = pipeline.getIsEmail();
        if (isEmail) {
            Integer[] emailStrategy = pipeline.getEmailStrategy();
            if (ArrayUtil.isNotEmpty(emailStrategy)) {
                if (StringUtil.isEmpty(creatorEmail)) {
                    // TODO 记录未发送的通知
                    return;
                }

                // 发送失败通知邮件
                if (ObjectUtil.equals(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED, pipelineHistory.getExecutionStatus())
                        && ArrayUtil.contains(emailStrategy, 0)) {
                    MyDataMail.notifyPipelineFailed(creatorEmail, project.getProjectName(), pipeline.getPipelineName());
                }
                // 发送成功通知邮件
                if (ObjectUtil.equals(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS, pipelineHistory.getExecutionStatus())
                        && ArrayUtil.contains(emailStrategy, 1)) {
                    MyDataMail.notifyPipelineSuccess(creatorEmail, project.getProjectName(), pipeline.getPipelineName());
                }
            }
        }

        // 判断流水线连续失败次数
        int failures = ObjectUtil.defaultIfNull(pipeline.getConsecutiveFailures(), 0);
        // 若不是手动执行，且失败次数过多，则自动结束病禁用定时和webhook 并邮件通知
        if (!MyDataConstant.JOB_TRIGGER_TYPE_MANUAL.equals(triggerType) && failures >= mydataConfig.getPipelineMaxFailureCount()) {
            // 结束定时执行
            pipeline.setIsSchedule(false);
            pipeline.setIsWebhook(false);
            if (StringUtil.isEmpty(creatorEmail)) {
                // TODO 记录未发送的通知
                return;
            }
            MyDataMail.notifyPipelineFailure(creatorEmail, project.getProjectName(), pipeline.getPipelineName());
            // 调整调度
            pipelineScheduler.update(pipelineId);
        }

        // 更新流水线
        pipelineService.updateById(pipeline);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String, Object> jobDataMap = context.getJobDetail().getJobDataMap();

        // 取出租户id
        String tenantId = (String) jobDataMap.remove(SysConstant.TENANT_ID);

        try {
            // 流水线id
            Long pipelineId = (Long) jobDataMap.remove(MyDataConstant.JOB_DATA_KEY_PIPELINE_ID);
            // 流水线出发类型
            Integer triggerType = (Integer) jobDataMap.remove(MyDataConstant.JOB_DATA_KEY_TRIGGER_TYPE);

            // 执行流水线
            execute(jobDataMap, pipelineId, triggerType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // throw new JobExecutionException(e);
        } finally {
            // 放回租户id
            jobDataMap.put(SysConstant.TENANT_ID, tenantId);
        }
    }

    @Override
    public void interrupt() {
        log.info("job interrupted");
        interrupted = true;
    }
}
