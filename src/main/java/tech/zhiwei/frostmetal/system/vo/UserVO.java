package tech.zhiwei.frostmetal.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

/**
 * 用户VO
 *
 * @author LIEN
 * @since 2024/8/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户")
public class UserVO extends BaseVO {
    @Schema(description = "编号")
    private String code;

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "手机")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "所属部门id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long departmentId;

    @Schema(description = "所属部门名称")
    private String departmentName;

    @Schema(description = "所属角色id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    @Schema(description = "所属角色名称")
    private String roleName;

    @Schema(description = "登录账号")
    private String loginName;

    @Schema(description = "头像")
    private String avatar;
}
