package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
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

        // json列表
        List<String> originJsonList = CollectionUtil.newArrayList();
        // 数据json列表
        List<String> dataJsonList = CollectionUtil.newArrayList();

        // json字符串转为json对象
        JSON originJson = JsonUtil.parse(originJsonString);

        // 提取业务数据json对象
        String fieldPrefix = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_PREFIX);
        JSON dataJson = (JSON) originJson.getByPath(StringUtil.nullToEmpty(fieldPrefix));
        log("数据所在层级：{}，提取的数据JSON：{}", fieldPrefix, dataJson);

        // 若没有数据，则结束
        if (dataJson instanceof JSONObject && ((JSONObject) dataJson).isEmpty()) {
            log("JSON为空 {}，结束执行。", dataJson.toString());
            return;
        } else if (dataJson instanceof JSONArray && ((JSONArray) dataJson).isEmpty()) {
            log("JSON为空 {}，结束执行。", dataJson.toString());
            return;
        }

        // 将json加入列表，待后续处理
        originJsonList.add(originJsonString);

        // 数据json加入列表
        dataJsonList.add(dataJson.toString());

        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String originJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_ORIGIN_JSON);
        if (StringUtil.isNotEmpty(originJsonKey)) {
            jobContextData.put(originJsonKey, originJsonList);
        }
        log("获取的原始JSON：{}", originJsonList);

        String dataJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_DATA_JSON);
        if (StringUtil.isNotEmpty(dataJsonKey)) {
            jobContextData.put(dataJsonKey, dataJsonList);
        }
        log("获取的数据JSON：{}", dataJsonList);
    }
}