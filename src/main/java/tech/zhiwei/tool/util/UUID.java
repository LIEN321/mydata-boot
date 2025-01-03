package tech.zhiwei.tool.util;

import java.io.Serial;

/**
 * UUID工具类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class UUID extends cn.hutool.core.lang.UUID {
    @Serial
    private static final long serialVersionUID = -4100578677243519419L;

    /**
     * 使用指定的数据构造新的 UUID。
     *
     * @param mostSigBits  用于 {@code UUID} 的最高有效 64 位
     * @param leastSigBits 用于 {@code UUID} 的最低有效 64 位
     */
    public UUID(long mostSigBits, long leastSigBits) {
        super(mostSigBits, leastSigBits);
    }
}
