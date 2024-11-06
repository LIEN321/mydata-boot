package tech.zhiwei.frostmetal.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.TreeVO;
import tech.zhiwei.tool.tree.TreeNode;

import java.util.List;

/**
 * 机构部门VO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "机构部门")
public class DepartmentVO extends TreeVO implements TreeNode<DepartmentVO> {
    @Schema(description = "机构名称")
    private String name;
    @Schema(description = "机构类型")
    private Integer type;
    @Schema(description = "机构排序")
    private Integer sort;
    @Schema(description = "机构备注")
    private String remark;
    @Schema(description = "子层级机构")
    private List<DepartmentVO> children;
}
