package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.entity.Role;
import tech.zhiwei.frostmetal.system.vo.RoleSelectVO;
import tech.zhiwei.frostmetal.system.vo.RoleVO;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构部门包装类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class RoleWrapper extends BaseWrapper<Role, RoleVO> {
    private RoleWrapper() {
    }

    public static RoleWrapper getInstance() {
        return new RoleWrapper();
    }

    @Override
    public RoleVO entityVO(Role entity) {
        return BeanUtil.copyProperties(entity, RoleVO.class);
    }

    public List<RoleSelectVO> selectVOList(List<Role> entityList) {
        List<RoleSelectVO> selectVOList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(entityList)) {
            for (Role role : entityList) {
                RoleSelectVO roleSelectVO = new RoleSelectVO();
                roleSelectVO.setLabel(role.getName());
                roleSelectVO.setValue(role.getId());
                selectVOList.add(roleSelectVO);
            }
        }
        return selectVOList;
    }
}
