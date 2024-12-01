package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;

import java.util.Map;

/**
 * 从Webhook解析数据
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Slf4j
public class GetJsonFromWebhook extends GetJsonFromApi {

    public GetJsonFromWebhook(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public void execute(Map<String, Object> jobContextData) {
        log.info("从Webhook解析数据 开始");

        PipelineTask pipelineTask = getPipelineTask();
        String jsonString = (String) jobContextData.get(MyDataConstant.JOB_DATA_KEY_API_BODY);
        String fieldPrefix = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_PREFIX);

        handleJson(jsonString, fieldPrefix, jobContextData);
    }
}