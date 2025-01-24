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
    public static PipelineJson pipelineJson(String json, String fieldPrefix) {
        // json字符串转为json对象
        JSON originJson = JsonUtil.parse(json);
        // 业务数据json集合
        List<JSON> dataJsonList = CollectionUtil.newArrayList();
        // 流水线json对象
        PipelineJson pipelineJson = new PipelineJson(originJson, dataJsonList);

        // 使用数组模式 兼容单个对象和数组模式
        JSONArray originJsonArray;
        if (originJson instanceof JSONArray) {
            originJsonArray = (JSONArray) originJson;
        } else {
            originJsonArray = new JSONArray();
            originJsonArray.add(originJson);
        }

        originJsonArray.forEach(obj -> {
            JSONObject jsonObject = (JSONObject) obj;
            JSON dataJson;
            if (StringUtil.isEmpty(fieldPrefix)) {
                dataJson = jsonObject;
            } else {
                dataJson = (JSON) jsonObject.getByPath(fieldPrefix);
            }
            dataJsonList.add(dataJson);
        });
        return pipelineJson;
    }

    /**
     * 判断流水线json是否没有数据
     *
     * @param pipelineJson 流水线json集合
     * @return 是否没有数据
     */
    public static boolean hasNoData(PipelineJson pipelineJson) {
        if (pipelineJson == null || CollectionUtil.isEmpty(pipelineJson.getDataJsonList())) {
            return true;
        }

        for (JSON json : pipelineJson.getDataJsonList()) {
            if (JsonUtil.isNotEmpty(json)) {
                return false;
            }
        }

        return true;
    }
}
