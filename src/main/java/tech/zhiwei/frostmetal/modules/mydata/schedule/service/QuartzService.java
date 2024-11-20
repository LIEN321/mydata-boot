package tech.zhiwei.frostmetal.modules.mydata.schedule.service;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;

/**
 * Quartz Service类
 *
 * @author LIEN
 * @since 2024/11/20
 */
@Service
public class QuartzService {
    private final Scheduler scheduler;

    public QuartzService() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
