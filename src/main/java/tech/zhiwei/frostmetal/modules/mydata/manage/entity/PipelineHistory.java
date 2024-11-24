package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;
import java.util.Date;

/**
 * 流水线执行记录 entity
 *
 * @author LIEN
 * @since 2024/11/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_history")
public class PipelineHistory extends TenantEntity {
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
     * 流水线参数
     */
    private String pipelineVars;

}