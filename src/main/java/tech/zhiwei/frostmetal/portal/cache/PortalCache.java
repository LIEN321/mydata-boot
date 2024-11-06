package tech.zhiwei.frostmetal.portal.cache;

import tech.zhiwei.frostmetal.cache.CacheUtil;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.portal.vo.MenuDataItem;

import java.util.List;

/**
 * Portal缓存
 *
 * @author LIEN
 * @since 2024/11/5
 */
public class PortalCache {

    // 系统模块的缓存统一前缀
    private static final String PORTAL_CACHE_PREFIX = SysConstant.CACHE_PREFIX + "portal";
    // 角色课件菜单的缓存前缀
    private static final String CACHE_ROLE_MENUS = "menu:role:id:";

    // ---------------------------------------- 菜单缓存 ----------------------------------------

    /**
     * 缓存角色可见的菜单列表
     *
     * @param roleId        角色id
     * @param menuDataItems 待缓存的菜单列表
     */
    public static void putRoleMenus(Long roleId, List<MenuDataItem> menuDataItems) {
        CacheUtil.put(PORTAL_CACHE_PREFIX, CACHE_ROLE_MENUS, roleId, menuDataItems);
    }

    /**
     * 获取角色可见的菜单列表
     *
     * @param roleId 角色id
     * @return 角色可见菜单列表
     */
    public static List<MenuDataItem> getRoleMenus(Long roleId) {
        return (List<MenuDataItem>) CacheUtil.get(PORTAL_CACHE_PREFIX, CACHE_ROLE_MENUS + roleId);
    }

    /**
     * 删除角色可见的菜单列表
     *
     * @param roleId 角色id
     */
    public static void removeRoleMenus(Long roleId) {
        CacheUtil.remove(PORTAL_CACHE_PREFIX, CACHE_ROLE_MENUS, roleId);
    }
}
