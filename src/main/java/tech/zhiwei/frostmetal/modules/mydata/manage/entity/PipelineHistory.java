package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;
import java.util.Date;
import java.util.Map;

/**
 * 流水线执行记录 entity
 *
 * @author LIEN
 * @since 2024/11/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_history", autoResultMap = true)
public class PipelineHistory extends TenantEntity {
    @Serial
    private static final long serialVersionUID = -7830690754588859083L;
    /**
     * 所属流水线
     */
    private Long pipelineId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 耗时秒数
     */
    private Long executionTime;

    /**
     * 触发方式
     */
    private Integer triggerType;

    /**
     * 触发时的参数
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> triggerParam;

    /**
     * 流水线参数
     */
    private String pipelineVars;

    /**
     * 执行状态
     */
    private Integer executionStatus;

    /**
     * 执行次数
     */
    private Integer executionCount;
}