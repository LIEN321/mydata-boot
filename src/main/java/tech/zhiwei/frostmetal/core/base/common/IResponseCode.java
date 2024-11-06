package tech.zhiwei.frostmetal.core.base.common;

import java.io.Serializable;

/**
 * 响应状态码
 *
 * @author LIEN
 * @since 2024/8/26
 */
public interface IResponseCode extends Serializable {
    String getMessage();

    int getCode();
}