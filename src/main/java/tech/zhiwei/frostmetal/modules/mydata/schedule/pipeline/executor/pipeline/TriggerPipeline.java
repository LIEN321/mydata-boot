package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline;

import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.ObjectUtil;
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

    public TriggerPipeline(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        List<PipelineJson> pipelineJsons = getPipelineJson(jobContextData);
        if (ObjectUtil.isEmpty(pipelineJsons)) {
            log("json内容为空，不触发流水线，结束执行。");
            return;
        }

        pipelineJsons.forEach(pipelineJson -> {
            Long targetPipelineId = NumberUtil.parseLong((String) getTaskConfig().get("PIPELINE_ID"));
            pipelineScheduler.webhookPipeline(pipelineTask.getTenantId(), targetPipelineId, pipelineJson.getOriginJson().toString());
            log("触发流水线成功，提交json={}", pipelineJson.getOriginJson());
        });
    }
}
