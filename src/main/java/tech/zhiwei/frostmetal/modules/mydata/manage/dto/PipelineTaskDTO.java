package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 流水线任务 DTO
 *
 * @author LIEN
 * @since 2024/11/16
 */
@Data
@Schema(description = "流水线任务")
public class PipelineTaskDTO {

    @Schema(description = "所属项目")
    private Long projectId;

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
    private Map<String, Object> taskConfig;

    @Schema(description = "业务状态")
    private Integer status;

    @Schema(description = "后续的前提条件")
    private Integer preCondition;

    @Schema(description = "失败重试次数，默认0：不重试")
    private Integer retry;
}
