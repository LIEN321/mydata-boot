package tech.zhiwei.tool.lang;

/**
 * 异常工具类
 *
 * @author LIEN
 * @since 2025/11/5
 */
public class ExceptionUtil extends cn.hutool.core.exceptions.ExceptionUtil {
    /**
     * 将指定的消息包装为运行时异常
     *
     * @param message 异常消息
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntime(String message, Object... params) {
        return new RuntimeException(StringUtil.format(message, params));
    }
}
