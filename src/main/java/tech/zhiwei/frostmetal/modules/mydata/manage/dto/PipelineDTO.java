package tech.zhiwei.frostmetal.modules.mydata.manage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

import java.util.List;


/**
 * 流水线 DTO
 *
 * @author LIEN
 * @since 2024/11/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线")
public class PipelineDTO extends BaseDTO {
    @Schema(description = "所属项目")
    private Long projectId;

    @Schema(description = "所属分组")
    private Long groupId;

    @Schema(description = "流水线名称")
    private String pipelineName;

    @Schema(description = "流水线描述")
    private String pipelineDesc;

    @Schema(description = "是否启用定时")
    private Boolean isSchedule;

    @Schema(description = "执行日")
    private Integer[] dayOfWeek;

    @Schema(description = "开始时间，HH:mm")
    private String startTime;

    @Schema(description = "结束时间，HH:mm")
    private String endTime;

    @Schema(description = "时间间隔，HH:mm:ss")
    private String intervalTime;

    @Schema(description = "时区")
    private String timeZone;

    @Schema(description = "是否启用webhook")
    private Integer isWebhook;

    @Schema(description = "webhook认证方式")
    private String webhookAuthType;

    @Schema(description = "webhook认证参数")
    private String webhookAuthParams;

    @Schema(description = "是否启用邮件")
    private Integer isEmail;

    @Schema(description = "邮件通知策略")
    private Integer[] emailStrategy;

    @Schema(description = "接收人")
    private String emailReceiver;

    @Schema(description = "任务编排列表")
    private List<PipelineTaskDTO> tasks;
}
