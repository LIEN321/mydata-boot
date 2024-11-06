package tech.zhiwei.tool.util;

/**
 * UUID工具类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class UUID {
    /**
     * 随机生成UUID
     *
     * @return UUID
     */
    public static String randomUUID() {
        return cn.hutool.core.lang.UUID.randomUUID().toString();
    }
}
