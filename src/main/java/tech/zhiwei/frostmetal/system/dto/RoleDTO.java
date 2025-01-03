package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.BaseDTO;

/**
 * 角色 DTO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色参数")
public class RoleDTO extends BaseDTO {
    @Schema(description = "角色编号")
    private String code;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色备注")
    private String remark;
}
