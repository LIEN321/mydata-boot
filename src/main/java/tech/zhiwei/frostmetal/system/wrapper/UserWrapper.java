package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.cache.SysCache;
import tech.zhiwei.frostmetal.system.entity.Department;
import tech.zhiwei.frostmetal.system.entity.Role;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.vo.UserVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 用户包装类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class UserWrapper extends BaseWrapper<User, UserVO> {
    private UserWrapper() {
    }

    public static UserWrapper getInstance() {
        return new UserWrapper();
    }

    @Override
    public UserVO entityVO(User entity) {
        // 用户信息
        UserVO userVO = BeanUtil.copyProperties(entity, UserVO.class);
        // 所属部门
        Department department = SysCache.getDepartment(entity.getDepartmentId());
        if (department != null) {
            userVO.setDepartmentName(department.getName());
        }
        // 所属角色
        Role role = SysCache.getRole(entity.getRoleId());
        if (role != null) {
            userVO.setRoleName(role.getName());
        }
        return userVO;
    }
}
