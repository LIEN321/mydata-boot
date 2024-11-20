package tech.zhiwei.frostmetal.modules.mydata.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.schedule.job.PipelineJob;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.util.ArrayUtil;

import java.time.LocalTime;
import java.util.Date;

/**
 * @author LIEN
 * @since 2024/11/20
 */
@Component
@Slf4j
public class ApplicationEventListener {
    /**
     * 程序启动时，查询已启动定时的流水线，并根据定时配置 加入调度
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws SchedulerException {
        log.info("onApplicationReady");

        // 创建 Scheduler
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // TODO 查询已启动定时的流水线

        JobDetail jobDetail = JobBuilder.newJob(PipelineJob.class)
                // TODO 设置流水线id
                .withIdentity("123456")
                .usingJobData("id", "1234567")
                .build();

        // TODO 根据流水线配置，构建Trigger
        Integer[] dayOfWeek = {2, 3, 4, 5, 6};
        String intervalTime = "00:01:00";
        // 计算间隔秒
        int intervalSeconds = LocalTime.parse(intervalTime).toSecondOfDay();
        // 计算下一次执行时间
        Date startTime = DateUtil.add(new Date(), null, null, intervalSeconds);

        // 指定执行的星期数
        WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
        if (ArrayUtil.isNotEmpty(dayOfWeek)) {
            for (Integer day : dayOfWeek) {
                weeklyCalendar.setDayExcluded(day, false);
            }
        }
        scheduler.addCalendar("weeklyCalendar", weeklyCalendar, false, false);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalSeconds)
                        .repeatForever())
                .startAt(startTime)
                .modifiedByCalendar("weeklyCalendar")
                .build();

        scheduler.start();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
