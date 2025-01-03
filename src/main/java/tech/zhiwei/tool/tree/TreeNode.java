package tech.zhiwei.tool.tree;

import java.util.List;

/**
 * 树节点
 *
 * @author LIEN
 * @since 2024/8/27
 */
public interface TreeNode<T> {
    /**
     * 主键id
     *
     * @return id
     */
    Long getId();

    /**
     * 父节点id
     *
     * @return 父节点id
     */
    Long getParentId();

    /**
     * 子节点列表
     *
     * @return 子节点列表
     */
    List<T> getChildren();

    /**
     * 设置子节点列表
     *
     * @param children 子节点列表
     */
    void setChildren(List<T> children);
}
