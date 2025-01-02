package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.system.dto.RoleDTO;
import tech.zhiwei.frostmetal.system.entity.Role;

import java.util.List;

/**
 * 角色 Service接口
 *
 * @author LIEN
 * @since 2024/8/27
 */
public interface IRoleService extends IBaseService<Role> {
    /**
     * 新增或更新角色
     *
     * @param roleDTO 角色数据
     * @return id
     */
    Long saveRole(RoleDTO roleDTO);

    /**
     * 在指定租户下 新增角色
     *
     * @param tenantId 租户id
     * @param roleDTO  角色信息
     * @return 角色id
     */
    Long saveTenantRole(String tenantId, RoleDTO roleDTO);

    /**
     * 为角色分配权限
     *
     * @param roleIds 角色id
     * @param menuIds 菜单id列表
     * @return true-成功，false-失败
     */
    boolean grant(List<Long> roleIds, List<Long> menuIds);
}
