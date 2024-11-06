package tech.zhiwei.frostmetal.system.util;

import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.tool.util.RandomUtil;

/**
 * 系统工具类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class SysUtil {
    /**
     * 生成加盐字符串
     *
     * @return 加盐字符串
     */
    public static String getSalt() {
        return RandomUtil.randomString(SysConstant.SALT_LENGTH);
    }

    /**
     * 生成随机密码
     * @return 密码
     */
    public static String getPassword() {
        return RandomUtil.randomString(SysConstant.PASSWORD_LENGTH);
    }
}
