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

            PipelineJson pipelineJson = new PipelineJson(baseJson, dataJson);
            pipelineJsons.add(pipelineJson);
        });
        return pipelineJsons;
    }

    /**
     * 判断流水线json是否没有数据
     *
     * @param pipelineJsons 流水线json集合
     * @return 是否没有数据
     */
    public static boolean isAllEmpty(List<PipelineJson> pipelineJsons) {
        if (CollectionUtil.isEmpty(pipelineJsons)) {
            return true;
        }

        for (PipelineJson pipelineJson : pipelineJsons) {
            if (JsonUtil.isNotEmpty(pipelineJson.getDataJson())) {
                return false;
            }
        }

        return true;
    }
}
