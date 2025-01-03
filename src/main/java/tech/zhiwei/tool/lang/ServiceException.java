package tech.zhiwei.tool.lang;

import java.io.Serial;

/**
 * 通用的业务异常
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class ServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 239277618035108414L;

    public ServiceException(String message) {
        super(message);
    }
}
