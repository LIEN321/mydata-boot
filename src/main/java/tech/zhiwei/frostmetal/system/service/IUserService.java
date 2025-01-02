package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.portal.dto.UserInfoDTO;
import tech.zhiwei.frostmetal.system.dto.UserDTO;
import tech.zhiwei.frostmetal.system.entity.User;

/**
 * 用户管理 Service接口
 *
 * @author LIEN
 * @since 2024/8/26
 */
public interface IUserService extends IBaseService<User> {
    /**
     * 新增或更新用户
     *
     * @param userDTO 用户信息
     * @return 用户id
     */
    Long saveUser(UserDTO userDTO);

    /**
     * 在指定租户下 新增用户
     *
     * @param tenantId 租户id
     * @param userDTO  用户信息
     * @return 用户id
     */
    Long saveTenantUser(String tenantId, UserDTO userDTO);

    /**
     * 更新用户基本信息
     *
     * @param userInfoDTO 基本信息
     * @return 操作结果：true-成功，false-失败
     */
    boolean updateBaseInfo(UserInfoDTO userInfoDTO);

    /**
     * 执行登录，校验登录信息
     *
     * @param tenantId  租户id
     * @param loginName 登录账号
     * @param password  登录密码
     * @return 用户对象，有效值-成功 null-失败
     */
    User verify(String tenantId, String loginName, String password);

    /**
     * 校验指定用户原密码，校验通过后设置新密码
     *
     * @param userId      用户id
     * @param oldPassword 校验的原密码
     * @param newPassword 新密码
     * @return 操作结果：true-成功，false-失败
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置指定用户密码
     *
     * @param userId 用户id
     * @return 新密码
     */
    String resetPassword(Long userId);

    /**
     * 统计部门下的用户数量
     *
     * @param departmentId 部门id
     * @return 部门下的用户数量
     */
    Long countByDepartment(Long departmentId);
}
