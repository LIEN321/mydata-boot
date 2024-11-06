package tech.zhiwei.frostmetal.portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.portal.cache.PortalCache;
import tech.zhiwei.frostmetal.portal.vo.MenuDataItem;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.service.IMenuService;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 框架全局Portal Controller
 *
 * @author LIEN
 * @since 2024/8/29
 */
@RestController
@RequestMapping("/portal")
@AllArgsConstructor
@Tag(name = "portal", description = "框架Portal API")
public class PortalController {

    private IMenuService menuService;

    private IUserService userService;

    /**
     * 查询当前登录用户的菜单
     */
    @GetMapping("/menus")
    @Operation(summary = "查询当前登录用户的菜单", operationId = "portalMenus")
    public R<List<MenuDataItem>> portalMenus() {
        // 当前用户的角色
        Long roleId = AuthUtil.getRoleId();

        // 先尝试从缓存读取角色可见菜单列表
        List<MenuDataItem> menuDataItems = PortalCache.getRoleMenus(roleId);
        if (menuDataItems != null) {
            return R.data(menuDataItems);
        }

        List<Menu> menus = menuService.listByRole(roleId);
        if (CollectionUtil.isEmpty(menus)) {
            return R.data(null);
        }

        // 将菜单集合 重新按层级排列
        menuDataItems = wrapperMenu(menus, SysConstant.DEFAULT_PARENT_ID);
        PortalCache.putRoleMenus(roleId, menuDataItems);
        return R.data(menuDataItems);
    }

    private List<MenuDataItem> wrapperMenu(Collection<Menu> menus, Long parentId) {
        List<Menu> subMenus;
        if (parentId == null) {
            subMenus = menus.stream().filter(m -> Objects.isNull(m.getParentId())).toList();
        } else {
            subMenus = menus.stream().filter(m -> Objects.equals(m.getParentId(), parentId)).toList();
        }

        List<MenuDataItem> menuDataItemList = CollectionUtil.newArrayList();
        subMenus.forEach(menu -> {
            MenuDataItem menuDataItem = new MenuDataItem();
            BeanUtil.copyProperties(menu, menuDataItem);
            menuDataItem.setKey(menu.getId().toString());
            menuDataItem.setChildren(wrapperMenu(menus, menu.getId()));
            menuDataItemList.add(menuDataItem);
        });

        return menuDataItemList.stream().sorted(Comparator.comparing(MenuDataItem::getSort)).toList();
    }
}
