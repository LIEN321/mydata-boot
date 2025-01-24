package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import cn.hutool.json.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
     * 原始json
     */
    private JSON originJson;

    /**
     * 数据json
     */
    private List<JSON> dataJsonList;
}
