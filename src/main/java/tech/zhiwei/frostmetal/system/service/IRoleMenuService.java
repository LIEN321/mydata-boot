package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IIdService;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;

import java.util.List;

/**
 * 角色菜单 Service接口
 *
 * @author LIEN
 * @since 2024/8/28
 */
public interface IRoleMenuService extends IIdService<RoleMenu> {

    /**
     * 根据角色id 查询关联的菜单列表
     *
     * @param roleId 角色id
     * @return 角色关联的菜单列表
     */
    List<RoleMenu> listByRole(Long roleId);

    /**
     * 根据角色id 查询关联的菜单列表
     *
     * @param roleIds 角色id列表
     * @return 角色关联的菜单列表
     */
    List<RoleMenu> listByRoles(List<Long> roleIds);

    /**
     * 根据菜单id 查询关联的角色列表
     *
     * @param menuId 菜单id
     * @return 角色列表
     */
    List<RoleMenu> listByMenuId(Long menuId);
}
