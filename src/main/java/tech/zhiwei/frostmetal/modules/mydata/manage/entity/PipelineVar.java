package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;

/**
 * 流水线变量 entity
 *
 * @author LIEN
 * @since 2025/10/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_var")
public class PipelineVar extends TenantEntity {
    @Serial
    private static final long serialVersionUID = -4637509779350938620L;
    
    /**
     * 所属流水线
     */
    private Long pipelineId;

    /**
     * 变量编号
     */
    private String varCode;

    /**
     * 变量值
     */
    private String varValue;

    /**
     * 变量值类型
     */
    private String varType;

    /**
     * 变量描述
     */
    private String varDesc;

    /**
     * 是否隐藏
     */
    private Integer isHide;

}