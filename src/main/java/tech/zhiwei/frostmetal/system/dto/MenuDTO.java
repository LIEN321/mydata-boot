package tech.zhiwei.frostmetal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.dto.TreeDTO;

/**
 * 菜单 DTO
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单参数")
public class MenuDTO extends TreeDTO {
    @Schema(description = "菜单编号")
    private String code;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "菜单排序")
    private Integer sort;
    
    @Schema(description = "菜单类型")
    private Integer type;

    @Schema(description = "菜单备注")
    private String remark;
}
