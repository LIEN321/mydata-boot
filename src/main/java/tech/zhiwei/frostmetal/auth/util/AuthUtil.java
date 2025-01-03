package tech.zhiwei.frostmetal.auth.util;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.auth.bean.AuthUser;

/**
 * 用户授权工具类
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Slf4j
public class AuthUtil {
    private static final String SESSION_KEY = "AUTH_USER";

    /**
     * 保存登录信息
     *
     * @param authUser 授权用户
     */
    public static void login(AuthUser authUser, Boolean autoLogin) {
        if (authUser != null) {
            StpUtil.login(authUser.getId(), autoLogin);
            StpUtil.getSession().set(SESSION_KEY, authUser);
        }
    }

    /**
     * 更新登录用户信息
     *
     * @param name   姓名
     * @param phone  电话
     * @param email  邮箱
     * @param avatar 头像
     */
    public static void update(String name, String phone, String email, String avatar) {
        AuthUser authUser = getUser();
        if (authUser == null) {
            return;
        }
        if (name != null) {
            authUser.setName(name);
        }
        if (phone != null) {
            authUser.setPhone(phone);
        }
        if (email != null) {
            authUser.setEmail(email);
        }
        if (avatar != null) {
            authUser.setAvatar(avatar);
        }

        StpUtil.getSession().set(SESSION_KEY, authUser);
    }

    /**
     * 退出登录
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 获取登录用户详情
     *
     * @return 用户详情
     */
    public static AuthUser getUser() {
        try {
            return (AuthUser) StpUtil.getSession().get(SESSION_KEY);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 获取当前登录用户id
     *
     * @return 用户id
     */
    public static Long getUserId() {
        AuthUser authUser = getUser();
        if (authUser != null) {
            return authUser.getId();
        }
        return null;
    }

    /**
     * 获取角色id
     *
     * @return 角色id
     */
    public static Long getRoleId() {
        AuthUser authUser = getUser();
        if (authUser != null) {
            return authUser.getRoleId();
        }
        return null;
    }

    /**
     * 获取所属租户id
     *
     * @return 所属租户id
     */
    public static String getTenantId() {
        AuthUser authUser = getUser();
        if (authUser != null) {
            return authUser.getTenantId();
        }
        return "";
    }
}
