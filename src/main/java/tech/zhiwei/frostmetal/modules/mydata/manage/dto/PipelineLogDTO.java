package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;
import java.util.Date;

/**
 * 流水线执行日志 DTO
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线执行日志")
public class PipelineLogDTO extends BaseDTO {
    @Schema(description = "所属流水线")
    private Long pipelineId;

    @Schema(description = "所属执行记录")
    private Long historyId;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "日志内容")
    private String taskLog;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "耗时")
    private Long executionTime;

    @Schema(description = "执行状态")
    private Integer executionStatus;

}
