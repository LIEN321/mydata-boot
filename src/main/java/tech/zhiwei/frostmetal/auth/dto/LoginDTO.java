package tech.zhiwei.frostmetal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 登录参数
 *
 * @author LIEN
 * @since 2024/9/1
 */
@Data
@Schema(description = "登录参数")
public class LoginDTO {
    @Schema(description = "租户编号")
    @NotEmpty(message = "租户编号不能为空")
    private String code;

    @Schema(description = "登录账号")
    @NotEmpty(message = "登录账号不能为空")
    private String username;

    @Schema(description = "登录密码")
    @NotEmpty(message = "登录密码不能为空")
    private String password;

    @Schema(description = "自动登录")
    private Boolean autoLogin = false;
}
