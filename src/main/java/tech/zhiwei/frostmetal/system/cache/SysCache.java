package tech.zhiwei.frostmetal.system.cache;

import tech.zhiwei.frostmetal.cache.CacheUtil;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.system.entity.Department;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.entity.Role;
import tech.zhiwei.frostmetal.system.entity.RoleMenu;
import tech.zhiwei.frostmetal.system.entity.SysApi;
import tech.zhiwei.frostmetal.system.service.IDepartmentService;
import tech.zhiwei.frostmetal.system.service.IMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleMenuService;
import tech.zhiwei.frostmetal.system.service.IRoleService;
import tech.zhiwei.frostmetal.system.service.ISysApiService;
import tech.zhiwei.frostmetal.system.service.ITenantService;
import tech.zhiwei.frostmetal.system.vo.DepartmentTreeVO;
import tech.zhiwei.frostmetal.system.vo.MenuTreeVO;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.List;

/**
 * 系统缓存
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class SysCache {
    private static final IDepartmentService departmentService;
    private static final IRoleService roleService;
    private static final IMenuService menuService;
    private static final IRoleMenuService roleMenuService;
    private static final ISysApiService sysApiService;
    private static final ITenantService tenantService;

    static {
        departmentService = SpringUtil.getBean(IDepartmentService.class);
        roleService = SpringUtil.getBean(IRoleService.class);
        menuService = SpringUtil.getBean(IMenuService.class);
        roleMenuService = SpringUtil.getBean(IRoleMenuService.class);
        sysApiService = SpringUtil.getBean(ISysApiService.class);
        tenantService = SpringUtil.getBean(ITenantService.class);
    }

    // 系统模块的缓存统一前缀
    private static final String SYS_CACHE_PREFIX = SysConstant.CACHE_PREFIX + "system";
    // department缓存前缀
    private static final String CACHE_DEPARTMENT = "department:id:";
    // 部门树缓存前缀
    private static final String CACHE_DEPARTMENT_TREE = "department:department_tree";
    // role缓存前缀
    private static final String CACHE_ROLE = "role:id:";
    // menu缓存前缀
    private static final String CACHE_MENU = "menu:id:";
    // 菜单树缓存前缀
    private static final String CACHE_MENU_TREE = "menu:menu_tree";
    // role_menu缓存前缀
    private static final String CACHE_ROLE_MENU_ID = "role_menu:menu_id:";
    // sys api缓存前缀
    private static final String CACHE_SYS_API = "sys_api:";
    // tenant缓存前缀
    private static final String CACHE_TENANT = "tenant:code:";

    // ---------------------------------------- 部门缓存 ----------------------------------------

    /**
     * 获取部门
     *
     * @param id 部门id
     * @return 部门
     */
    public static Department getDepartment(Long id) {
        return CacheUtil.get(SYS_CACHE_PREFIX, CACHE_DEPARTMENT, id, () -> departmentService.getById(id));
    }

    /**
     * 缓存部门树
     *
     * @return 菜单树
     */
    public static void putDepartmentTree(List<DepartmentTreeVO> departmentTree) {
        CacheUtil.put(SYS_CACHE_PREFIX, CACHE_DEPARTMENT_TREE, departmentTree);
    }

    /**
     * 删除缓存的部门树
     */
    public static void removeDepartmentTree() {
        CacheUtil.remove(SYS_CACHE_PREFIX, CACHE_DEPARTMENT_TREE);
    }

    /**
     * 获取部门树
     *
     * @return 部门树
     */
    public static List<DepartmentTreeVO> getDepartmentTree() {
        return (List<DepartmentTreeVO>) CacheUtil.get(SYS_CACHE_PREFIX, CACHE_DEPARTMENT_TREE);
    }

    // ---------------------------------------- 角色缓存 ----------------------------------------

    /**
     * 获取角色
     *
     * @param id 角色id
     * @return 角色
     */
    public static Role getRole(Long id) {
        return CacheUtil.get(SYS_CACHE_PREFIX, CACHE_ROLE, id, () -> roleService.getById(id));
    }

    // ---------------------------------------- 菜单缓存 ----------------------------------------

    /**
     * 获取菜单
     *
     * @param id 菜单id
     * @return 菜单
     */
    public static Menu getMenu(Long id) {
        return CacheUtil.get(SYS_CACHE_PREFIX, CACHE_MENU, id, () -> menuService.getById(id));
    }

    /**
     * 缓存菜单管理的菜单树
     */
    public static void putMenuTree(List<MenuTreeVO> menuTree) {
        CacheUtil.put(SYS_CACHE_PREFIX, CACHE_MENU_TREE, menuTree);
    }

    /**
     * 删除菜单管理缓存的菜单树
     */
    public static void removeMenuTree() {
        CacheUtil.remove(SYS_CACHE_PREFIX, CACHE_MENU_TREE);
    }

    /**
     * 获取菜单管理的菜单树
     *
     * @return 菜单树
     */
    public static List<MenuTreeVO> getMenuTree() {
        return (List<MenuTreeVO>) CacheUtil.get(SYS_CACHE_PREFIX, CACHE_MENU_TREE);
    }

    // ---------------------------------------- 角色菜单缓存 ----------------------------------------

    /**
     * 获取菜单关联角色的配置列表
     *
     * @param menuId 菜单id
     * @return 菜单关联角色的配置列表
     */
    public static List<RoleMenu> getRoleMenuByMenuId(Long menuId) {
        return CacheUtil.get(SYS_CACHE_PREFIX, CACHE_ROLE_MENU_ID, menuId, () -> roleMenuService.listByMenuId(menuId));
    }

    /**
     * 删除菜单关联角色的配置缓存
     *
     * @param menuId 菜单id
     */
    public static void removeRoleMenuByMenuId(Long menuId) {
        CacheUtil.remove(SYS_CACHE_PREFIX, CACHE_ROLE_MENU_ID, menuId);
    }

    // ---------------------------------------- 接口缓存 ----------------------------------------

    /**
     * 根据method和path获取唯一的系统接口
     *
     * @param method 请求方法
     * @param path   请求地址
     * @return 系统接口
     */
    public static SysApi getSysApi(String method, String path) {
        return CacheUtil.get(SYS_CACHE_PREFIX, CACHE_SYS_API, method + ":" + path, () -> sysApiService.findByRequest(method, path));
    }
}
