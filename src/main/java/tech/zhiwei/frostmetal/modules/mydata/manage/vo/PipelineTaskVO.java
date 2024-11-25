package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.Map;

/**
 * 流水线任务 VO
 *
 * @author LIEN
 * @since 2024/11/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线任务")
public class PipelineTaskVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -8071599184776939188L;

    @Schema(description = "所属项目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    @Schema(description = "所属流水线")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pipelineId;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "关联应用")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    @Schema(description = "关联API")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long apiId;

    @Schema(description = "关联数据")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataId;

    @Schema(description = "任务配置")
    private Map<String, Object> taskConfig;

    @Schema(description = "数据仓库名称")
    private String warehouse;

}
