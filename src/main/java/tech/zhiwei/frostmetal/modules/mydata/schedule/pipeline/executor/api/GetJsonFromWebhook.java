package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobJsonService;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * 从Webhook接收JSON
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Slf4j
public class GetJsonFromWebhook extends TaskExecutor {

    public GetJsonFromWebhook(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();
        String originJsonString = (String) jobContextData.get(MyDataConstant.JOB_DATA_KEY_WEBHOOK_REQUEST_BODY);
        log("从Webhook接收的json：{}", originJsonString);

        // 提取业务数据json对象
        String fieldPrefix = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_PREFIX);

        // 将json字符串转为流水线json对象
        List<PipelineJson> pipelineJsons = JobJsonService.pipelineJson(originJsonString, fieldPrefix);

        // 若没有数据，则结束
        if (CollectionUtil.isEmpty(pipelineJsons)) {
            log("JSON为空 {}，结束执行。");
            return;
        }

        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String pipelineJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_PIPELINE_JSON);
        if (StringUtil.isNotEmpty(pipelineJsonKey)) {
            jobContextData.put(pipelineJsonKey, pipelineJsons);
        }
        log("从Webhook接收JSON完成");
    }
}