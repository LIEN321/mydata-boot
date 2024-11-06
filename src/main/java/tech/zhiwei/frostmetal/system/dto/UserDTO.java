package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 用户 DTO
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户参数")
public class UserDTO extends BaseDTO {
    @Schema(description = "编号")
    private String code;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "所属部门id")
    private Long departmentId;

    @Schema(description = "角色id")
    private Long roleId;

    @Schema(description = "登录账号")
    private String loginName;

    @Schema(description = "登录密码")
    private String loginPassword;

    @Schema(description = "头像")
    private String avatar;
}
