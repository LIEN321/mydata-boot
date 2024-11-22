package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import jakarta.annotation.Resource;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.quartz.QuartzService;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;

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
            stopPipeline(pipelineId);
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
        // 计算下一次执行时间
        // TODO 临时改用null
//        Date startTime = DateUtil.add(new Date(), null, null, intervalSeconds);
        Date startTime = null;

        try {
            // 使用 quartz 调度任务
            quartzService.scheduleJob(pipelineId, dayOfWeek, intervalSeconds, startTime, MdConstant.JOB_REPEAT_FOREVER);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止流水线的调度
     *
     * @param pipelineId 流水线id
     */
    public void stopPipeline(Long pipelineId) {
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
