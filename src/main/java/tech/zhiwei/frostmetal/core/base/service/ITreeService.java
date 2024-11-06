package tech.zhiwei.frostmetal.core.base.service;

import tech.zhiwei.frostmetal.core.base.entity.TreeEntity;

import java.util.List;

/**
 * 层级结构Tree Service
 *
 * @author LIEN
 * @since 2024/8/27
 */
public interface ITreeService<T extends TreeEntity> extends IBaseService<T> {

    /**
     * 根据父记录id 查询下一级记录列表
     * 若父级id无效，则返回根节点列表
     *
     * @param parentId 父记录id
     * @return 下一级记录列表
     */
    List<T> listByParentId(Long parentId);

    /**
     * 根据父记录id 查询所有子层级记录
     * 若父级id无效，则返回null
     *
     * @param parentId 父记录id
     * @return 所有子层级记录
     */
    List<T> listAllByParentId(Long parentId);
}
