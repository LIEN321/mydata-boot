package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;
import java.util.Date;

/**
 * 流水线执行日志 VO
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Schema(description = "流水线执行日志")
public class PipelineLogVO extends BaseVO {
    @Schema(description = "所属流水线")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pipelineId;

    @Schema(description = "所属执行记录")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long historyId;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "日志内容")
    private String taskLog;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Schema(description = "耗时")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executionTime;

    @Schema(description = "执行状态")
    private Integer executionStatus;

}
