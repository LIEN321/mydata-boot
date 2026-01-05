package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineJob;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.NumberUtil;

import java.util.List;
import java.util.Map;

/**
 * 触发流水线
 *
 * @author LIEN
 * @since 2024/12/15
 */
@Slf4j
public class TriggerPipeline extends TaskExecutor {

    private final PipelineScheduler pipelineScheduler = SpringUtil.getBean(PipelineScheduler.class);

    // public TriggerPipeline(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        // 是否同步执行流水线，默认true
        Boolean isSync = (Boolean) pipelineTask.getTaskConfig().getOrDefault("SYNC", true);

        List<PipelineJson> pipelineJsons = getPipelineJson(jobContextData);
        if (ObjectUtil.isEmpty(pipelineJsons)) {
            info("json内容为空，不触发流水线，结束执行。");
            return;
        }

        pipelineJsons.forEach(pipelineJson -> {
            Long targetPipelineId = NumberUtil.parseLong((String) getTaskConfig().get("PIPELINE_ID"));
            // 流水线json
            String pipelineJsonString = pipelineJson.getOriginJson().toString();
            if (isSync) {
                // 同步执行流水线
                PipelineJob pipelineJob = new PipelineJob();
                try {
                    Map<String, Object> map = MapUtil.newHashMap();
                    map.put(MyDataConstant.JOB_DATA_KEY_WEBHOOK_REQUEST_BODY, pipelineJsonString);
                    info("同步触发流水线开始，提交json={}", pipelineJson.getOriginJson());
                    pipelineJob.execute(map, targetPipelineId, MyDataConstant.JOB_TRIGGER_TYPE_WEBHOOK);
                    info("同步触发流水线结束");
                } catch (JobExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // 异步触发流水线
                pipelineScheduler.webhookPipeline(pipelineTask.getTenantId(), targetPipelineId, pipelineJsonString);
                info("异步触发流水线成功，提交json={}", pipelineJson.getOriginJson());
            }
        });
    }
}
