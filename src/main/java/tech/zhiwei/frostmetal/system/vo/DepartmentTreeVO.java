package tech.zhiwei.frostmetal.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.TreeVO;
import tech.zhiwei.tool.tree.TreeNode;

import java.io.Serial;
import java.util.List;

/**
 * 部门树 VO
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentTreeVO extends TreeVO implements TreeNode<DepartmentTreeVO> {

    @Serial
    private static final long serialVersionUID = 7797337492577898790L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long value;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long key;

    private String title;
    private List<DepartmentTreeVO> children;
}
