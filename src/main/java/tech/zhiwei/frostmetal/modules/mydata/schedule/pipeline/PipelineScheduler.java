package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import jakarta.annotation.Resource;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
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
}
