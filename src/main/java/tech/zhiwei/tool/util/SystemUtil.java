package tech.zhiwei.tool.util;

/**
 * 系统工具类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class SystemUtil {
    /**
     * 获取 user.dir 目录
     *
     * @return user.dir 目录
     */
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }
}
