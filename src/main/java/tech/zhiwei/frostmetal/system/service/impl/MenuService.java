package tech.zhiwei.frostmetal.system.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.TreeService;
import tech.zhiwei.frostmetal.system.dto.MenuDTO;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.mapper.MenuMapper;
import tech.zhiwei.frostmetal.system.service.IMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleMenuService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringPool;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Set;

/**
 * 菜单 Service实现类
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Service
@AllArgsConstructor
public class MenuService extends TreeService<MenuMapper, Menu> implements IMenuService {

    private IRoleMenuService roleMenuService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveMenu(MenuDTO menuDTO) {
        Menu menu = BeanUtil.copyProperties(menuDTO, Menu.class);
        saveOrUpdate(menu);
        return menu.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeMenu(Long id) {
        Menu menu = getById(id);
        List<Long> menuIds = CollectionUtil.newArrayList();
        if (menu != null) {
            menuIds.add(id);
            // 查询所有子层级菜单
            List<Menu> menus = listAllByParentId(id);
            if (CollectionUtil.isNotEmpty(menus)) {
                List<Long> subMenuIds = menus.stream().map(Menu::getId).toList();
                menuIds.addAll(subMenuIds);
            }
        }
        return removeByIds(menuIds);
    }

    @Override
    public List<Menu> listByRole(Long roleId) {
        // 根据角色查询关联菜单
        List<RoleMenu> roleMenus = roleMenuService.listByRole(roleId);
        if (CollectionUtil.isEmpty(roleMenus)) {
            return null;
        }
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).toList();

        // 查询菜单列表
        List<Menu> menus = listByIds(menuIds);

        // 不重复的菜单id
        Set<Long> uniqueMenuIds = CollectionUtil.newHashSet(menuIds);

        // 拼接所有菜单的 treePath，提取所有父级菜单id
        StringBuilder treePath = new StringBuilder();
        for (Menu menu : menus) {
            String idTreePath = menu.getIdTreePath();
            if (StringUtil.isNotEmpty(idTreePath)) {
                treePath.append(idTreePath).append(StringPool.COMMA);
            }
        }
        if (!treePath.isEmpty()) {
            Long[] ids = StringUtil.splitToLong(treePath.toString());
            uniqueMenuIds.addAll(CollectionUtil.toList(ids));
        }

        // 查询用户可见菜单
        return listByIds(uniqueMenuIds);
    }
}
