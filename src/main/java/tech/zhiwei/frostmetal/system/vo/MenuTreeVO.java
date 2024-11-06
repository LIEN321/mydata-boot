package tech.zhiwei.frostmetal.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.zhiwei.frostmetal.core.base.vo.TreeVO;
import tech.zhiwei.tool.tree.TreeNode;

import java.util.List;

/**
 * 菜单 VO
 *
 * @author LIEN
 * @since 2024/8/28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTreeVO extends TreeVO implements TreeNode<MenuTreeVO> {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long value;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long key;

    private String title;

    private List<MenuTreeVO> children;
}
