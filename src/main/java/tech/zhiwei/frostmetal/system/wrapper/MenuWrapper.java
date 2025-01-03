package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.vo.MenuTreeVO;
import tech.zhiwei.frostmetal.system.vo.MenuVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;

/**
 * 菜单包装类
 *
 * @author LIEN
 * @since 2024/8/28
 */
public class MenuWrapper extends BaseWrapper<Menu, MenuVO> {
    private MenuWrapper() {
    }

    public static MenuWrapper getInstance() {
        return new MenuWrapper();
    }

    @Override
    public MenuVO entityVO(Menu entity) {
        return BeanUtil.copyProperties(entity, MenuVO.class);
    }

    public List<MenuTreeVO> menuTreeVOList(List<Menu> entityList) {
        List<MenuTreeVO> menuTreeVOList = CollectionUtil.newArrayList();
        entityList.forEach(menu -> {
            MenuTreeVO menuTree = BeanUtil.copyProperties(menu, MenuTreeVO.class);
            menuTree.setValue(menu.getId());
            menuTree.setKey(menu.getId());
            menuTree.setTitle(menu.getName());
            menuTreeVOList.add(menuTree);
        });

        return menuTreeVOList;
    }
}
