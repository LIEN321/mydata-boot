package tech.zhiwei.frostmetal.modules.mydata.schedule.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 流水线Job
 *
 * @author LIEN
 * @since 2024/11/20
 */
@Component
@Slf4j
public class PipelineJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("PipelineJob execute");
//        log.info((String) context.get("id")); // return null
//        log.info((String) context.getJobDetail().getJobDataMap().get("id")); // return id value
    }
}
