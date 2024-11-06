package tech.zhiwei.frostmetal.portal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 框架菜单VO
 *
 * @author LIEN
 * @since 2024/8/29
 */
@Data
@Schema(description = "菜单")
public class MenuDataItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 5816959839881611947L;

    @Schema(description = "菜单id")
    private String key;

    @Schema(description = "菜单名称")
    private String name;
    
    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单路径")
    private String path;

    @Schema(description = "菜单排序")
    private Integer sort;

    @Schema(description = "菜单类型")
    private Integer type;

    @Schema(description = "子菜单")
    private List<MenuDataItem> children;
}
