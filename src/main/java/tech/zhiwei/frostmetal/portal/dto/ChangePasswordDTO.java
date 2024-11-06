package tech.zhiwei.frostmetal.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改密码 DTO
 *
 * @author LIEN
 * @since 2024/8/30
 */
@Data
@Schema(description = "修改密码参数")
public class ChangePasswordDTO {
    @Schema(description = "原密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "确认密码")
    private String confirmPassword;
}
