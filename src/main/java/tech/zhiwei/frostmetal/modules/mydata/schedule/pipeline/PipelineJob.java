package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.spring.SpringUtil;

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
public class PipelineJob implements Job {
    private final IPipelineService pipelineService = SpringUtil.getBean(IPipelineService.class);
    private final IPipelineTaskService pipelineTaskService = SpringUtil.getBean(IPipelineTaskService.class);

    // Job执行过程中的变量
    private Map<String, Object> jobData = new HashMap<String, Object>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("PipelineJob execute");
        // TODO 流水线的执行内容
        Long pipelineId = context.getJobDetail().getJobDataMap().getLong(MdConstant.JOB_DATA_KEY_PIPELINE_ID);

        Pipeline pipeline = pipelineService.getById(pipelineId);
        if (pipeline == null) {
            throw new JobExecutionException(StringUtil.format("Job执行失败：流水线不存在，id={}！", pipelineId));
        }

        // TODO 创建流水线的执行记录

        // 查询流水线任务列表
        List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);
        if (CollectionUtil.isEmpty(tasks)) {
            return;
        }

        tasks.forEach(task -> {
            // TODO 记录执行过程log
            // 执行任务
            Object taskResult = TaskExecutor.getExecutor(task).execute();
            // TODO 临时用task的type作为key
            jobData.put(task.getTaskType(), taskResult);
            log.info(jobData.toString());
        });
    }
}
