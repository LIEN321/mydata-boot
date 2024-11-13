package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;


/**
 * 流水线分组 entity
 *
 * @author LIEN
 * @since 2024/11/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_pipeline_group")
public class PipelineGroup extends TenantEntity {
    /**
     * 所属项目
     */
    private Long projectId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组描述
     */
    private String groupDesc;

}