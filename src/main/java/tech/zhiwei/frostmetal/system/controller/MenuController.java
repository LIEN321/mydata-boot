package tech.zhiwei.frostmetal.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.dto.MenuDTO;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.service.IMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleMenuService;
import tech.zhiwei.frostmetal.system.vo.MenuTreeVO;
import tech.zhiwei.frostmetal.system.vo.MenuVO;
import tech.zhiwei.frostmetal.system.wrapper.MenuWrapper;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.tree.TreeUtil;

import java.util.Collection;
import java.util.List;

/**
 * 菜单管理 Controller
 *
 * @author LIEN
 * @since 2024/8/28
 */
@RestController
@RequestMapping("/menu")
@AllArgsConstructor
@Tag(name = "menu", description = "菜单管理API")
public class MenuController {
    private IMenuService menuService;

    private IRoleMenuService roleMenuService;

    @PostMapping
    @Operation(summary = "新增或更新菜单", operationId = "saveMenu")
    public R<Long> save(@RequestBody MenuDTO menuDTO) {
        try {
            return R.data(menuService.saveMenu(menuDTO));
        } finally {
            // 删除缓存
            SysCache.removeMenuTree();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询菜单", operationId = "menuList")
    public R<List<MenuVO>> list(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Menu> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtil.isNotEmpty(code), Menu::getCode, code)
                .like(StringUtil.isNotEmpty(name), Menu::getName, name)
                .orderByAsc(Menu::getSort);
        List<MenuVO> list = MenuWrapper.getInstance().listVO(menuService.list(queryWrapper));
        return R.data(TreeUtil.convert(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "菜单详情", operationId = "menuDetail")
    @Parameter(name = "id", description = "记录id")
    public R<MenuVO> detail(@PathVariable Long id) {
        return R.data(MenuWrapper.getInstance().entityVO(menuService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除菜单", operationId = "deleteMenu")
    @Parameter(name = "id", description = "记录id")
    public R<Boolean> delete(@PathVariable Long id) {
        try {
            return R.status(menuService.removeMenu(id));
        } finally {
            // 删除缓存
            SysCache.removeMenuTree();
        }
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜单", operationId = "deleteMenus")
    public R<Boolean> delete(@RequestBody Collection<Long> ids) {
        try {
            AssertUtil.notEmpty(ids, "请选择要删除的记录");
            ids.forEach(id -> {
                menuService.removeMenu(id);
            });
            return R.status(true);

        } finally {
            // 删除缓存
            SysCache.removeMenuTree();
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "菜单目录树", operationId = "menuTree")
    public List<MenuTreeVO> menuTree() {
        Long roleId = AuthUtil.getRoleId();
        // 查询当前用户角色的菜单
        List<Menu> menus = menuService.listByRole(roleId);
        if (CollectionUtil.isEmpty(menus)) {
            return null;
        }

        List<MenuTreeVO> menuTreeList = MenuWrapper.getInstance().menuTreeVOList(menus);
        menuTreeList = TreeUtil.convert(menuTreeList);

        return menuTreeList;
    }

    @GetMapping("/listByRole/{roleId}")
    @Operation(summary = "查询角色关联的菜单", operationId = "menuListByRole")
    public List<String> menuListByRole(@PathVariable Long roleId) {
        // 根据角色查询关联菜单
        List<RoleMenu> roleMenus = roleMenuService.listByRole(roleId);
        if (CollectionUtil.isNotEmpty(roleMenus)) {
            // 提取关联的菜单id
            List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).toList();
            // 根据菜单id 查询叶子节点
            LambdaQueryWrapper<Menu> queryWrapper =
                    Wrappers.<Menu>lambdaQuery().in(Menu::getId, menuIds).eq(Menu::getIsLeaf, SysConstant.STATUS_ENABLED);

            List<Menu> menus = menuService.list(queryWrapper);
            return menus.stream().map(Menu::getId).map(Object::toString).toList();
        }

        return CollectionUtil.newArrayList();
    }
}
