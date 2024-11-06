package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.entity.Menu;
import tech.zhiwei.frostmetal.system.entity.SysApi;
import tech.zhiwei.frostmetal.system.vo.SysApiVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 系统接口包装类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class SysApiWrapper extends BaseWrapper<SysApi, SysApiVO> {
    private SysApiWrapper() {
    }

    public static SysApiWrapper getInstance() {
        return new SysApiWrapper();
    }

    @Override
    public SysApiVO entityVO(SysApi entity) {
        SysApiVO sysApiVO = BeanUtil.copyProperties(entity, SysApiVO.class);
        // 所属菜单
        Menu menu = SysCache.getMenu(entity.getMenuId());
        if (menu != null) {
            sysApiVO.setMenuName(menu.getName());
        }
        return sysApiVO;
    }
}
