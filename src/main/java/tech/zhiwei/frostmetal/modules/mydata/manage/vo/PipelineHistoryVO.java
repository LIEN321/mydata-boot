package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.Date;

/**
 * 流水线执行记录 VO
 *
 * @author LIEN
 * @since 2024/11/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线执行记录")
public class PipelineHistoryVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = 5689609414870192713L;
    @Schema(description = "所属流水线")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pipelineId;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Schema(description = "耗时秒数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executionTime;

    @Schema(description = "触发方式")
    private Integer triggerType;

    @Schema(description = "流水线参数")
    private String pipelineVars;

    @Schema(description = "执行状态")
    private Integer executionStatus;
}
