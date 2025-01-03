package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.tenant.entity.TenantEntity;

import java.io.Serial;

/**
 * 角色 Entity
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_ROLE)
public class Role extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 2101847620374072561L;
    /**
     * 角色编号
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色备注
     */
    private String remark;
}
