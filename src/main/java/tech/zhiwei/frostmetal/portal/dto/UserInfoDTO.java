package tech.zhiwei.frostmetal.portal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户个人信息 DTO
 *
 * @author LIEN
 * @since 2024/8/30
 */
@Data
@Schema(description = "用户个人信息")
public class UserInfoDTO {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;
}
