package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.webhook;

import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobJsonService;
import tech.zhiwei.tool.collection.CollectionUtil;

/**
 * 从Webhook接收JSON
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Slf4j
public class GetJsonFromWebhook extends TaskExecutor {

    // public GetJsonFromWebhook(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        PipelineTask pipelineTask = getPipelineTask();
        String originJsonString = (String) pipelineContext.get(MyDataConstant.JOB_DATA_KEY_WEBHOOK_REQUEST_BODY);
        info("从Webhook接收的json：{}", originJsonString);

        // 提取业务数据json对象
        String fieldPrefix = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_PREFIX);

        // 将json字符串转为流水线json对象
        PipelineJson pipelineJson = JobJsonService.pipelineJson(originJsonString, fieldPrefix);

        // 将结果保存到 job上下文
        setPipelineJson(pipelineContext, CollectionUtil.toList(pipelineJson));
        info("从Webhook接收JSON完成");
    }
}