package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.portal.dto.UserInfoDTO;
import tech.zhiwei.frostmetal.system.dto.UserDTO;
import tech.zhiwei.frostmetal.system.entity.User;
import tech.zhiwei.frostmetal.system.mapper.UserMapper;
import tech.zhiwei.frostmetal.system.service.IUserService;
import tech.zhiwei.frostmetal.system.util.SysUtil;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ServiceException;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.secure.SecureUtil;
import tech.zhiwei.tool.text.PasswordStrength;

/**
 * 用户管理 Service实现类
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Service
public class UserService extends BaseService<UserMapper, User> implements IUserService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveUser(UserDTO userDTO) {
        return saveTenantUser(null, userDTO);
    }

    @Override
    public Long saveTenantUser(String tenantId, UserDTO userDTO) {
        // 校验信息
        checkUserInfo(tenantId, userDTO.getId(), userDTO.getLoginName());

        User user = BeanUtil.copyProperties(userDTO, User.class);
        user.setTenantId(tenantId);

        // 更新salt，加密登录密码
        if (StringUtil.isNotBlank(user.getLoginPassword())) {
            String salt = SysUtil.getSalt();
            user.setLoginPassword(encryptPassword(user.getLoginPassword(), salt));
            user.setSalt(salt);
        }

        saveOrUpdate(user);
        return user.getId();
    }

    @Override
    public boolean updateBaseInfo(UserInfoDTO userInfoDTO) {
        User user = BeanUtil.copyProperties(userInfoDTO, User.class);
        return updateById(user);
    }

    @Override
    public User verify(String tenantId, String loginName, String password) {
        AssertUtil.notBlank(tenantId, "租户无效！");
        AssertUtil.notBlank(loginName, "账号不能为空！");
        AssertUtil.notBlank(password, "密码不能为空！");

        Wrapper<User> userWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getLoginName, loginName)
                .eq(User::getTenantId, tenantId);

        User user = this.getOne(userWrapper);
        if (user != null && encryptPassword(password, user.getSalt()).equals(user.getLoginPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = getById(userId);
        AssertUtil.notNull(user, "用户信息已失效！");
        // 用户有效 且 原密码正确
        AssertUtil.equals(encryptPassword(oldPassword, user.getSalt()), user.getLoginPassword(), "原密码不正确");

        // 设置新密码
        setNewPassword(user, newPassword);

        return updateById(user);
    }

    @Override
    public String resetPassword(Long userId) {
        User user = getById(userId);
        AssertUtil.notNull(user, "用户不存在！");

        // 生成新的随机密码
        String newPassword = SysUtil.getPassword();

        // 设置新密码
        setNewPassword(user, newPassword);

        // 更新用户
        updateById(user);

        // 返回新密码
        return newPassword;
    }

    @Override
    public Long countByDepartment(Long departmentId) {
        AssertUtil.notNull(departmentId, "参数departmentId无效");

        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery().eq(User::getDepartmentId, departmentId);
        return count(queryWrapper);
    }

    /**
     * 密码加密处理
     *
     * @param password 明文密码
     * @param salt     密码salt
     * @return 加密后的密码
     */
    private String encryptPassword(String password, String salt) {
        String data = password + salt;
        return StringUtil.isBlank(data) ? "" : SecureUtil.sha256(SecureUtil.md5(data));
    }

    /**
     * 校验用户信息有效性
     *
     * @param tenantId  租户id
     * @param userId    用户id
     * @param loginName 登录名
     */
    private void checkUserInfo(String tenantId, Long userId, String loginName) {
        if (StringUtil.isNotEmpty(loginName)) {
            // 校验账号是否重复
            Long loginNameCount = baseMapper.selectCount(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getLoginName, loginName)
                            .ne(userId != null, User::getId, userId)
                            .eq(StringUtil.isNotEmpty(tenantId), User::getTenantId, tenantId)
            );
            if (loginNameCount > 0L) {
                throw new ServiceException(StringUtil.format("登录账号 {} 已存在！", loginName));
            }
        }
    }

    /**
     * 设置新的登录密码
     *
     * @param user        用户
     * @param newPassword 新明文密码
     */
    private void setNewPassword(User user, String newPassword) {
        // 生成新的salt
        String newSalt = SysUtil.getSalt();
        // 加密新密码
        String encryptedNewPassword = encryptPassword(newPassword, newSalt);

        // 更新用户
        user.setSalt(newSalt);
        user.setLoginPassword(encryptedNewPassword);
        user.setPasswordStrength(PasswordStrength.getStrengthLevel(newPassword));
    }
}
