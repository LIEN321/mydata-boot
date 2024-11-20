package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.jackson.IntegerArrayTypeHandler;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;


/**
 * 流水线 entity
 *
 * @author LIEN
 * @since 2024/11/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline")
public class Pipeline extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 7478639346585117850L;
    /**
     * 所属项目
     */
    private Long projectId;

    /**
     * 所属分组
     */
    private Long groupId;

    /**
     * 流水线名称
     */
    private String pipelineName;

    /**
     * 流水线描述
     */
    private String pipelineDesc;

    /**
     * 是否启用定时
     */
    private Boolean isSchedule;

    /**
     * 执行日
     */
    @TableField(typeHandler = IntegerArrayTypeHandler.class)
    private Integer[] dayOfWeek;

    /**
     * 开始时间，HH:mm
     */
    private String startTime;

    /**
     * 结束时间，HH:mm
     */
    private String endTime;

    /**
     * 时间间隔，HH:mm:ss
     */
    private String intervalTime;

    /**
     * 时区
     */
    private String timeZone;

    /**
     * 是否启用webhook
     */
    private Integer isWebhook;

    /**
     * webhook认证方式
     */
    private String webhookAuthType;

    /**
     * webhook认证参数
     */
    private String webhookAuthParams;

    /**
     * 是否启用邮件
     */
    private Integer isEmail;

    /**
     * 邮件通知策略
     */
    private Integer[] emailStrategy;

    /**
     * 接收人
     */
    private String emailReceiver;

}