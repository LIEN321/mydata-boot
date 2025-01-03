package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.ITreeService;
import tech.zhiwei.frostmetal.system.dto.MenuDTO;
import tech.zhiwei.frostmetal.system.entity.Menu;

import java.util.List;

/**
 * 菜单 Service接口
 *
 * @author LIEN
 * @since 2024/8/28
 */
public interface IMenuService extends ITreeService<Menu> {
    /**
     * 新增或更新菜单
     *
     * @param MenuDTO 菜单数据
     * @return id
     */
    Long saveMenu(MenuDTO MenuDTO);

    /**
     * 删除菜单
     *
     * @param id 菜单id
     * @return 操作结果，true-成功，false-失败
     */
    boolean removeMenu(Long id);

    /**
     * 根据角色查询菜单列表
     *
     * @param roleId 角色id
     * @return 菜单列表
     */
    List<Menu> listByRole(Long roleId);

    /**
     * 根据编号集合 查询菜单列表
     *
     * @param codes 编号集合
     * @return 菜单列表
     */
    List<Menu> listByCodes(List<String> codes);
}
