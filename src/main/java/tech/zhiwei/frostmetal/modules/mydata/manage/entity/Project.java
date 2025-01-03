package tech.zhiwei.frostmetal.modules.mydata.manage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;


/**
 * 项目 entity
 *
 * @author LIEN
 * @since 2024/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "md_project")
public class Project extends TenantEntity {
    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDesc;

}