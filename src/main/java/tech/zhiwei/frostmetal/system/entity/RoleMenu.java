package tech.zhiwei.frostmetal.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;

/**
 * 角色菜单 Entity
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(SysConstant.TABLE_SYS_ROLE_MENU)
public class RoleMenu extends IdEntity {
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 菜单id
     */
    private Long menuId;
}
