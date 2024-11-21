package tech.zhiwei.frostmetal.modules.mydata.schedule;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;

/**
 * @author LIEN
 * @since 2024/11/20
 */
@Component
@Slf4j
public class ApplicationEventListener {

    @Resource
    private IPipelineService pipelineService;

    @Resource
    private PipelineScheduler pipelineScheduler;

    /**
     * 程序启动时，查询已启动定时的流水线，并根据定时配置 加入调度
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws SchedulerException {
        log.info("onApplicationReady");

        // 查询已启动定时的流水线
        List<Pipeline> pipelines = pipelineService.listScheduledPipelines();
        if (CollectionUtil.isNotEmpty(pipelines)) {
            pipelineScheduler.schedulePipelines(pipelines);
        }
    }
}
