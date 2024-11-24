package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;
import java.util.Date;

/**
 * 流水线执行记录 DTO
 *
 * @author LIEN
 * @since 2024/11/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线执行记录")
public class PipelineHistoryDTO extends BaseDTO {
    @Schema(description = "所属流水线")
    private Long pipelineId;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "耗时秒数")
    private Long executionTime;

    @Schema(description = "触发方式")
    private Integer triggerType;

    @Schema(description = "流水线参数")
    private String pipelineVars;

}
