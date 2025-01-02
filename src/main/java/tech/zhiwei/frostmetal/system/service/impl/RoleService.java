package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.portal.cache.PortalCache;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.dto.RoleDTO;
import tech.zhiwei.frostmetal.system.entity.Role;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.mapper.RoleMapper;
import tech.zhiwei.frostmetal.system.service.IRoleMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色 Service实现类
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Service
@AllArgsConstructor
public class RoleService extends BaseService<RoleMapper, Role> implements IRoleService {

    private IRoleMenuService roleMenuService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveRole(RoleDTO roleDTO) {
        return saveTenantRole(null, roleDTO);
    }

    @Override
    public Long saveTenantRole(String tenantId, RoleDTO roleDTO) {
        Role role = BeanUtil.copyProperties(roleDTO, Role.class);
        role.setTenantId(tenantId);
        saveOrUpdate(role);
        return role.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean grant(List<Long> roleIds, List<Long> menuIds) {
        // 删除原菜单的缓存
        List<RoleMenu> oldRoleMenus = roleMenuService.listByRoles(roleIds);
        if (CollectionUtil.isNotEmpty(oldRoleMenus)) {
            Set<Long> oldMenuIds = oldRoleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
            oldMenuIds.forEach(SysCache::removeRoleMenuByMenuId);
        }

        // 删除角色可见的菜单缓存
        roleIds.forEach(PortalCache::removeRoleMenus);

        // 删除角色现有权限
        roleMenuService.remove(Wrappers.<RoleMenu>update().lambda().in(RoleMenu::getRoleId, roleIds));

        // 生成新权限
        List<RoleMenu> roleMenus = CollectionUtil.newArrayList();
        roleIds.forEach(roleId -> {
            menuIds.forEach(menuId -> {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenus.add(roleMenu);
            });
        });

        return roleMenuService.saveBatch(roleMenus);
    }
}
