package tech.zhiwei.frostmetal.core.base.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 层级结构实体
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TreeEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -1432996058508449183L;
    /**
     * 父记录id
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long parentId;

    /**
     * id层级路径
     */
    private String idTreePath;

    /**
     * 是否叶子节点
     */
    private Integer isLeaf;
}
