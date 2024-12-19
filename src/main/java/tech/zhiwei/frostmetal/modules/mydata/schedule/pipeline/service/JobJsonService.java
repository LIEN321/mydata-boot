package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;

/**
 * 任务中JSON的处理类
 *
 * @author LIEN
 * @since 2024/12/19
 */
public class JobJsonService {
    public static List<PipelineJson> pipelineJson(String json, String fieldPrefix) {
        List<PipelineJson> pipelineJsons = CollectionUtil.newArrayList();

        // json字符串转为json对象
        JSON originJson = JsonUtil.parse(json);
        // 使用数组模式 兼容单个对象和数组模式
        JSONArray originJsonArray;
        if (originJson instanceof JSONArray) {
            originJsonArray = (JSONArray) originJson;
        } else {
            originJsonArray = new JSONArray();
            originJsonArray.add(originJson);
        }

        originJsonArray.forEach(originJsonObject -> {
            JSONObject baseJson = (JSONObject) originJsonObject;
            JSON dataJson;
            if (StringUtil.isEmpty(fieldPrefix)) {
                dataJson = baseJson;
            } else {
                dataJson = (JSON) baseJson.getByPath(fieldPrefix);
            }

//            log("数据所在层级：{}，提取的数据JSON：{}", fieldPrefix, dataJson);

            // 使用数组模式 兼容单个对象和数组模式
            JSONArray dataJsonArray;
            if (dataJson instanceof JSONArray) {
                dataJsonArray = (JSONArray) dataJson;
            } else {
                dataJsonArray = new JSONArray();
                dataJsonArray.add(dataJson);
            }

            PipelineJson pipelineJson = new PipelineJson(baseJson, dataJsonArray);
            pipelineJsons.add(pipelineJson);
        });
        return pipelineJsons;
    }
}
