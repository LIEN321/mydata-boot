package tech.zhiwei.frostmetal.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.BaseVO;

/**
 * 角色VO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色")
public class RoleVO extends BaseVO {
    @Schema(description = "角色编号")
    private String code;

    @Schema(description = "角色姓名")
    private String name;

    @Schema(description = "角色备注")
    private String remark;
}
