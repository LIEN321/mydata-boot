package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.NumberUtil;

import java.util.Map;

/**
 * 用Webhook触发流水线
 *
 * @author LIEN
 * @since 2024/12/15
 */
@Slf4j
public class WebhookCallPipeline extends TaskExecutor {

    private final PipelineScheduler pipelineScheduler = SpringUtil.getBean(PipelineScheduler.class);

    public WebhookCallPipeline(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        // 输入配置
        Map<String, String> inputMap = getInputMap();

        // 获取待解析json的key
        String dataJsonKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_DATA_JSON);
        if (StringUtil.isEmpty(dataJsonKey)) {
            error("JSON变量名为空，结束执行。");
            throw new IllegalArgumentException("JSON变量名为空，结束执行。");
        }

        String dataJson = (String) jobContextData.get(dataJsonKey);
        JSON json = JsonUtil.parse(dataJson);
        if (json instanceof JSONObject jsonObject) {
            if (jsonObject.isEmpty()) {
                log("json内容为空，不触发流水线，结束执行。");
                return;
            }
        }
        if (json instanceof JSONArray jsonArray) {
            if (jsonArray.isEmpty()) {
                log("json内容为空，不触发流水线，结束执行。");
                return;
            }
        }

        Long targetPipelineId = NumberUtil.parseLong((String) getTaskConfig().get("PIPELINE_ID"));
        pipelineScheduler.webhookPipeline(targetPipelineId, dataJson);
        log("触发流水线成功，提交json={}", dataJson);
    }
}
