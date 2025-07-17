package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import java.io.Serial;

/**
 * 正常停止流水线
 *
 * @author LIEN
 * @since 2025/7/15
 */
public class StopPipelineException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 4531901540727214555L;

    public StopPipelineException() {
    }

    public StopPipelineException(String message) {
        super(message);
    }
}
