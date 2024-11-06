package tech.zhiwei.frostmetal.auth.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 认证信息
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@Schema(description = "认证信息")
public class AuthUser {
    @Schema(description = "所属租户")
    private String tenantId;

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "用户编号")
    private String code;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "用户名")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "所属部门id")
    private Long departmentId;

    @Schema(description = "手机")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "密码复杂度")
    private Integer passwordStrength;

    @Schema(description = "角色id")
    private Long roleId;
}
