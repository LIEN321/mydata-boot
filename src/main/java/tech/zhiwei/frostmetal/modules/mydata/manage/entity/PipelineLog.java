package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 流水线执行日志 entity
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_log")
public class PipelineLog extends IdEntity {
    @Serial
    private static final long serialVersionUID = 5341583702152848678L;
    /**
     * 所属流水线
     */
    private Long pipelineId;

    /**
     * 所属执行记录
     */
    private Long historyId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 日志内容
     */
    private String taskLog;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 耗时
     */
    private Long executionTime;

    /**
     * 执行状态
     */
    private Integer executionStatus;

    /**
     * 执行次数
     */
    private Integer executionCount;
}