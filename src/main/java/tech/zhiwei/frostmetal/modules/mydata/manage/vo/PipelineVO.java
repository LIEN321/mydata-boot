package tech.zhiwei.frostmetal.modules.mydata.manage.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

import java.io.Serial;
import java.util.Date;
import java.util.List;


/**
 * 流水线 VO
 *
 * @author LIEN
 * @since 2024/11/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "流水线")
public class PipelineVO extends BaseVO {
    @Serial
    private static final long serialVersionUID = -6039282082043520610L;
    @Schema(description = "所属项目")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long projectId;

    @Schema(description = "所属分组")
    @JsonSerialize(using = ToStringSerializer.class)
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

    @Schema(description = "流水线任务列表")
    private List<PipelineTaskVO> tasks;

    @Schema(description = "下次执行时间")
    private Date nextFireTime;

    @Schema(description = "最新一次的执行记录")
    private PipelineHistoryVO latestHistory;
}
