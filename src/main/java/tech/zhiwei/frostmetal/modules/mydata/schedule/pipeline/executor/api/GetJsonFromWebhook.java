package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
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
        String originJsonString = (String) jobContextData.get(MyDataConstant.JOB_DATA_KEY_API_BODY);
        String fieldPrefix = (String) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_PREFIX);

//        handleJson(CollectionUtil.toList(jsonString), fieldPrefix, jobContextData);

        // json列表
        List<String> originJsonList = CollectionUtil.newArrayList();
        // 数据json列表
        List<String> dataJsonList = CollectionUtil.newArrayList();

        // json字符串转为json对象
        JSON originJson = JsonUtil.parse(originJsonString);

        // 提取业务数据json对象
        JSON dataJson = (JSON) originJson.getByPath(StringUtil.nullToEmpty(fieldPrefix));
        // 若没有数据，则结束
        if (dataJson instanceof JSONObject && ((JSONObject) dataJson).isEmpty()) {
            return;
        } else if (dataJson instanceof JSONArray && ((JSONArray) dataJson).isEmpty()) {
            return;
        }

        // 将json加入列表，待后续处理
        originJsonList.add(originJsonString);

        // 数据json加入列表
        dataJsonList.add(dataJson.toString());

        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String originJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_ORIGIN_JSON);
        if (StringUtil.isNotEmpty(originJsonKey)) {
            jobContextData.put(originJsonKey, originJsonList);
        }
        String dataJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_DATA_JSON);
        if (StringUtil.isNotEmpty(dataJsonKey)) {
            jobContextData.put(dataJsonKey, dataJsonList);
        }
    }
}