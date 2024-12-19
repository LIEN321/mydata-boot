package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 流水线中的业务数据JSON
 *
 * @author LIEN
 * @since 2024/12/19
 */
@Data
@AllArgsConstructor
public class PipelineJson {
    /**
     * 原始json对象
     */
    private JSONObject originJson;

    /**
     * 数据json数组
     */
    private JSONArray dataJsons;
}
