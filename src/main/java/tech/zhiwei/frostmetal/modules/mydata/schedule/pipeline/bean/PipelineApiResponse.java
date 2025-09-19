package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 流水线Api响应包装类
 *
 * @author LIEN
 * @since 2025/9/3
 */
@Data
@AllArgsConstructor
public class PipelineApiResponse {
    private int status;
    private String data;
    private String cookie;
}
