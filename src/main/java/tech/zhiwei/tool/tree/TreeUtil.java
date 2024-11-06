package tech.zhiwei.tool.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;

/**
 * 树形数据工具类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class TreeUtil {
    public static <T extends TreeNode<T>> List<T> convert(List<T> nodes) {
        ImmutableMap<Long, T> nodeMap = Maps.uniqueIndex(nodes, TreeNode::getId);
        // 根节点列表
        List<T> rootNodes = CollectionUtil.newArrayList();
        nodes.forEach(node -> {
            if (node.getParentId() == null) {
                rootNodes.add(node);
            } else {
                TreeNode<T> parentNode = nodeMap.get(node.getParentId());
                if (parentNode != null) {
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(CollectionUtil.newArrayList());
                    }
                    parentNode.getChildren().add(node);
                } else {
                    rootNodes.add(node);
                }
            }
        });
        return rootNodes;
    }
}
