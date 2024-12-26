package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流水线中的业务数据JSON
 *
 * @author LIEN
 * @since 2024/12/19
 */
@Data
@AllArgsConstructor
public class PipelineJson implements Serializable {
    @Serial
    private static final long serialVersionUID = 3367393394544398794L;
    /**
     * 原始json对象
     */
    private JSONObject originJson;

    /**
     * 数据json数组
     */
    private JSONArray dataJsons;
}
