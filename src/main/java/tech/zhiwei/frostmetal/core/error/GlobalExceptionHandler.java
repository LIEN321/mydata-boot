package tech.zhiwei.frostmetal.core.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.core.base.common.ResponseCode;
import tech.zhiwei.tool.lang.ServiceException;
import tech.zhiwei.tool.lang.StringUtil;

/**
 * 全局统一处理异常
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 异常
     *
     * @param e ServiceException
     * @return R
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Object> handleException(ServiceException e) {
        log.error("业务异常", e);
        return R.fail(e.getMessage());
    }

    /**
     * 参数校验异常
     *
     * @param ex MethodArgumentNotValidException
     * @return R
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 提取第一个错误信息
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = (fieldError != null) ? fieldError.getDefaultMessage() : "校验错误";

        // 返回精简的错误信息
        return R.fail(ResponseCode.PARAM_VALID_ERROR, errorMessage);
    }

    /**
     * 全局Throwable异常
     *
     * @param e Throwable
     * @return R
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Object> handleError(Throwable e) {
        log.error("服务器异常", e);
        return R.fail(ResponseCode.INTERNAL_SERVER_ERROR, (StringUtil.isEmpty(e.getMessage()) ? ResponseCode.INTERNAL_SERVER_ERROR.getMessage() : e.getMessage()));
    }
}
