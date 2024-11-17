package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 流水线任务 DTO
 *
 * @author LIEN
 * @since 2024/11/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线任务")
public class PipelineTaskDTO extends BaseDTO {
    @Schema(description = "所属流水线")
    private Long pipelineId;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "关联应用")
    private Long appId;

    @Schema(description = "关联API")
    private Long apiId;

    @Schema(description = "关联数据")
    private Long dataId;

    @Schema(description = "任务配置")
    private String taskConfig;

}
