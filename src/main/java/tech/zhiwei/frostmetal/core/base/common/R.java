package tech.zhiwei.frostmetal.core.base.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求响应
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@Schema(description = "请求响应")
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -4857181313928754241L;

    // 响应状态码
    @Schema(description = "响应状态码")
    private int code;

    // 是否成功
    @Schema(description = "true-成功，false-失败")
    private boolean success;

    // 响应消息
    @Schema(description = "响应提示消息")
    private String message;

    // 响应数据
    @Schema(description = "响应数据")
    private T data;

    protected R() {
    }

    private R(IResponseCode responseCode) {
        this(responseCode.getCode(), null, responseCode.getMessage());
    }

    private R(IResponseCode responseCode, String message) {
        this(responseCode.getCode(), null, message);
    }

    private R(IResponseCode responseCode, T data) {
        this(responseCode.getCode(), data, responseCode.getMessage());
    }

    private R(IResponseCode responseCode, T data, String message) {
        this(responseCode.getCode(), data, message);
    }

    protected R(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.success = ResponseCode.SUCCESS.getCode() == code;
    }

    /**
     * 成功返回数据
     *
     * @param data 数据
     * @param <T>  泛型
     * @return 响应结果
     */
    public static <T> R<T> data(T data) {
        return new R<>(ResponseCode.SUCCESS.getCode(), data, ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 操作成功，无数据返回
     *
     * @param <T> 泛型
     * @return 响应结果
     */
    public static <T> R<T> success() {
        return new R<>(ResponseCode.SUCCESS);
    }

    /**
     * 操作成功，无数据返回
     *
     * @param <T> 泛型
     * @return 响应结果
     */
    public static <T> R<T> success(String message) {
        return new R<>(ResponseCode.SUCCESS, message);
    }

    /**
     * 操作失败，无数据返回
     *
     * @param <T> 泛型
     * @return 响应结果
     */
    public static <T> R<T> fail() {
        return new R<>(ResponseCode.FAILURE);
    }

    /**
     * 操作失败，无数据返回
     *
     * @param message 错误消息
     * @param <T>     泛型
     * @return 响应结果
     */
    public static <T> R<T> fail(String message) {
        return new R<>(ResponseCode.FAILURE, message);
    }

    /**
     * 操作失败，无数据返回
     *
     * @param code    响应状态码
     * @param message 错误消息
     * @param <T>     泛型
     * @return 响应结果
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, null, message);
    }

    /**
     * 操作失败，无数据返回
     *
     * @param responseCode 响应状态码
     * @param <T>          泛型
     * @return 响应结果
     */
    public static <T> R<T> fail(IResponseCode responseCode) {
        return new R<>(responseCode);
    }

    /**
     * 操作失败，无数据返回
     *
     * @param responseCode 响应状态码
     * @param message      错误消息
     * @param <T>          泛型
     * @return 响应结果
     */
    public static <T> R<T> fail(IResponseCode responseCode, String message) {
        return new R<>(responseCode, message);
    }

    /**
     * 根据操作结果 返回响应
     *
     * @param isSuccess 是否成功
     * @param <T>       泛型
     * @return 响应结果
     */
    public static <T> R<T> status(boolean isSuccess) {
        return isSuccess ? success() : fail();
    }
}
