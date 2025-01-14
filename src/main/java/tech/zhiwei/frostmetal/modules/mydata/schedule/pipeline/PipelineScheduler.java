package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import jakarta.annotation.Resource;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.quartz.QuartzService;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.util.RandomUtil;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 全局流水线的调度器
 * 启动、停止、删除流水线对应的任务
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Component
public class PipelineScheduler {
    @Resource
    private QuartzService quartzService;
    @Resource
    private IPipelineService pipelineService;

    /**
     * 更新流水线的调度
     *
     * @param pipelineId 流水线id
     */
    public void update(Long pipelineId) {
        Pipeline pipeline = pipelineService.getById(pipelineId);
        if (pipeline == null) {
            stopPipeline(pipelineId);
            return;
        }
        // 获取定时执行的状态
        boolean isSchedule = pipeline.getIsSchedule();
        if (isSchedule) {
            // 若启用，则加入调度
            schedulePipeline(pipeline);
        } else {
            // 若未启用，则移出调度
            deletePipeline(pipelineId);
        }
    }

    /**
     * 调度多个流水线
     *
     * @param pipelines 流水线集合
     */
    public void schedulePipelines(Collection<Pipeline> pipelines) {
        pipelines.forEach(this::schedulePipeline);
    }

    /**
     * 调度单个流水线
     *
     * @param pipeline 流水线
     */
    public void schedulePipeline(Pipeline pipeline) {
        if (pipeline == null) {
            return;
        }

        // 流水线id
        Long pipelineId = pipeline.getId();
        // 定时的执行日
        Integer[] dayOfWeek = pipeline.getDayOfWeek();
        // 间隔时间
        String intervalTime = pipeline.getIntervalTime();
        // 计算间隔秒
        int intervalSeconds = LocalTime.parse(intervalTime).toSecondOfDay();
        // 开始时间
        String startTime = pipeline.getStartTime();
        Date dStartTime = DateUtil.parse(startTime, MyDataConstant.TASK_TIME_FORMAT);
        dStartTime = DateUtil.updateTime(dStartTime, null, null, RandomUtil.randomInt(60));
        // 结束时间
        String endTime = pipeline.getEndTime();
        Date dEndTime = DateUtil.parse(endTime, MyDataConstant.TASK_TIME_FORMAT);

        try {
            // 使用 quartz 调度任务
            quartzService.scheduleJob(pipeline.getTenantId(), pipelineId, dayOfWeek, intervalSeconds, dStartTime, dEndTime, MyDataConstant.JOB_REPEAT_FOREVER);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 手动执行流水线
     *
     * @param tenantId   租户标识
     * @param pipelineId 流水线id
     */
    public void executePipeline(String tenantId, Long pipelineId) {
        try {
            quartzService.executeJob(tenantId, pipelineId, MyDataConstant.JOB_GROUP_MANUAL, MyDataConstant.JOB_TRIGGER_TYPE_MANUAL);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * webhook执行流水线
     *
     * @param tenantId   租户标识
     * @param pipelineId 流水线id
     */
    public void webhookPipeline(String tenantId, Long pipelineId, String body) {
        try {
            Map<String, Object> map = MapUtil.newHashMap();
            map.put(MyDataConstant.JOB_DATA_KEY_WEBHOOK_REQUEST_BODY, body);
            quartzService.executeJob(tenantId, pipelineId, MyDataConstant.JOB_GROUP_WEBHOOK, MyDataConstant.JOB_TRIGGER_TYPE_WEBHOOK, map);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止执行中的流水线
     *
     * @param pipelineId 流水线id
     */
    public void stopPipeline(Long pipelineId) {
        try {
            quartzService.stopPipeline(pipelineId);
        } catch (UnableToInterruptJobException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 撤销流水线的调度
     *
     * @param pipelineId 流水线id
     */
    public void deletePipeline(Long pipelineId) {
        quartzService.deleteJob(pipelineId);
    }

    /**
     * 获取流水线的下次执行时间
     *
     * @param pipelineId 流水线id
     * @return 下次执行时间
     */
    public Date getNextFireTime(Long pipelineId) {
        return quartzService.getNextFireTime(pipelineId);
    }
}
