package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import tech.zhiwei.frostmetal.core.base.service.IdService;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.mapper.RoleMenuMapper;
import tech.zhiwei.frostmetal.system.service.IRoleMenuService;

import java.util.List;

/**
 * 角色菜单 Service实现类
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Service
public class RoleMenuService extends IdService<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

    @Override
    public List<RoleMenu> listByRole(Long roleId) {
        Wrapper<RoleMenu> queryWrapper = Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getRoleId, roleId);
        return list(queryWrapper);
    }

    @Override
    public List<RoleMenu> listByRoles(List<Long> roleIds) {
        Wrapper<RoleMenu> queryWrapper = Wrappers.<RoleMenu>lambdaQuery()
                .in(RoleMenu::getRoleId, roleIds);
        return list(queryWrapper);
    }

    @Override
    public List<RoleMenu> listByMenuId(Long menuId) {
        Wrapper<RoleMenu> queryWrapper = Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getMenuId, menuId);
        return list(queryWrapper);
    }
}
