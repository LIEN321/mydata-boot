package tech.zhiwei.frostmetal.modules.mydata.schedule.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.springframework.stereotype.Service;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineJob;
import tech.zhiwei.tool.util.ArrayUtil;

import java.util.Date;

/**
 * Quartz Service类
 *
 * @author LIEN
 * @since 2024/11/20
 */
@Service
public class QuartzService {
    /**
     * 调度器，初步使用单个
     */
    private final Scheduler scheduler;

    public QuartzService() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    /**
     * 获取调度器
     */
    private Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * 加入调度任务
     *
     * @throws SchedulerException
     */
    public void scheduleJob(Long pipelineId, Integer[] dayOfWeek, int intervalSeconds, Date startTime, int repeatCount) throws SchedulerException {
        // 流水线id的字符串值
        String sPipelineId = pipelineId.toString();

        // 获取当前用户可用的调度器
        Scheduler currentScheduler = getScheduler();

        // 构建Job Detail对象，带有流水线id值
        JobDetail jobDetail = JobBuilder.newJob(PipelineJob.class)
                .withIdentity(sPipelineId)
                .usingJobData(MdConstant.JOB_DATA_KEY_PIPELINE_ID, pipelineId)
                .build();

        // 设置执行日 weeklyCalendar
        WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
        if (ArrayUtil.isNotEmpty(dayOfWeek)) {
            for (Integer day : dayOfWeek) {
                weeklyCalendar.setDayExcluded(day, false);
            }
        }

        // weeklyCalendar 添加入到schedule中
        currentScheduler.addCalendar(sPipelineId, weeklyCalendar, true, true);

        // 配置Trigger Builder
        TriggerBuilder<SimpleTrigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(sPipelineId)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalSeconds)
                        .withRepeatCount(repeatCount))
                .modifiedByCalendar(sPipelineId);
        if (startTime != null) {
            triggerBuilder.startAt(startTime);
        }

        // 构建Trigger
        Trigger trigger = triggerBuilder.build();

        // 通知 quartz 调度
        boolean isJobExist = currentScheduler.checkExists(new JobKey(sPipelineId));
        if (isJobExist) {
            currentScheduler.rescheduleJob(new TriggerKey(sPipelineId), trigger);
        } else {
            currentScheduler.scheduleJob(jobDetail, trigger);
        }
    }

    public void deleteJob(Long pipelineId) {
        // 流水线id的字符串值
        String sPipelineId = pipelineId.toString();
        // 获取当前用户可用的调度器
        Scheduler currentScheduler = getScheduler();
        try {
            currentScheduler.deleteJob(new JobKey(sPipelineId));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取Trigger的下次执行时间
     *
     * @param pipelineId Trigger标识
     * @return 下次执行时间
     */
    public Date getNextFireTime(Long pipelineId) {
        // 流水线id的字符串值
        String sPipelineId = pipelineId.toString();
        // 获取当前用户可用的调度器
        Scheduler currentScheduler = getScheduler();
        try {
            Trigger trigger = currentScheduler.getTrigger(new TriggerKey(sPipelineId));
            if (trigger != null) {
                return trigger.getNextFireTime();
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
