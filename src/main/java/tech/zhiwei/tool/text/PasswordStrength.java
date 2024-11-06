package tech.zhiwei.tool.text;

import cn.hutool.core.text.PasswdStrength;

/**
 * 检测密码强度
 *
 * @author LIEN
 * @since 2024/9/2
 */
public class PasswordStrength extends PasswdStrength {

    /**
     * 获取密码强度的5个等级数：1~5
     *
     * @param password 明文密码
     * @return 密码强度等级数
     */
    public static int getStrengthLevel(String password) {
        PASSWD_LEVEL level = getLevel(password);
        switch (level) {
            case EASY -> {
                return 1;
            }
            case MIDIUM -> {
                return 2;
            }
            case STRONG -> {
                return 3;
            }
            case VERY_STRONG -> {
                return 4;
            }
            default -> {
                return 5;
            }
        }
    }
}
