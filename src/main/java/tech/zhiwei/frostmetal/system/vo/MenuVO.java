package tech.zhiwei.frostmetal.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.TreeVO;
import tech.zhiwei.tool.tree.TreeNode;

import java.io.Serial;
import java.util.List;

/**
 * 菜单VO
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单")
public class MenuVO extends TreeVO implements TreeNode<MenuVO> {
    @Serial
    private static final long serialVersionUID = 8573937422552043442L;
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
    @Schema(description = "子层级菜单")
    private List<MenuVO> children;
}
