package tech.zhiwei.frostmetal.core.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.entity.TreeEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Collection;
import java.util.List;

/**
 * 层级结构Tree Service实现类
 *
 * @author LIEN
 * @since 2024/8/27
 */
public class TreeService<M extends BaseMapper<T>, T extends TreeEntity> extends BaseService<M, T>
        implements ITreeService<T> {

    @Override
    public boolean save(T entity) {
        parseTree(entity);
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        entityList.forEach(this::parseTree);
        return super.saveBatch(entityList, batchSize);
    }

    @Override
    public boolean updateById(T entity) {
        parseTree(entity);
        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        entityList.forEach(this::parseTree);
        return super.updateBatchById(entityList, batchSize);
    }

    @Override
    public boolean saveOrUpdate(T entity) {
        if (entity.getId() == null) {
            return this.save(entity);
        } else {
            return this.updateById(entity);
        }
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize) {
        entityList.forEach(this::parseTree);
        return super.saveOrUpdateBatch(entityList, batchSize);
    }

    private void parseTree(T entity) {
        if (ObjectUtil.isNull(entity)) {
            return;
        }

        // 若parentId为null，则不处理
        Long parentId = entity.getParentId();
        if (parentId == null) {
            return;
        }

        // 用于拼接id_tree_path的集合
        List<Object> list = CollectionUtil.newArrayList();
        // 查询父记录
        T parent = getById(parentId);
        // 若父记录有效，则先拼接父记录的 id_tree_path
        if (parent != null) {
            if (parent.getIdTreePath() != null) {
                list.add(parent.getIdTreePath());
            }

            // 更新parent记录的is_leaf为0
            parent.setIsLeaf(SysConstant.STATUS_DISABLED);
            updateById(parent);
        }
        // 再拼接当前记录的 parent_id
        list.add(parentId);

        // 执行拼接
        String idTreePath = StringUtil.join(list);
        entity.setIdTreePath(idTreePath);
    }

    @Override
    public List<T> listByParentId(Long parentId) {
        LambdaQueryWrapper<T> wrapper = Wrappers.lambdaQuery();
        if (parentId != null) {
            wrapper.eq(T::getParentId, parentId);
        } else {
            wrapper.isNull(T::getParentId);
        }
        return list();
    }

    @Override
    public List<T> listAllByParentId(Long parentId) {
        if (parentId == null) {
            return null;
        }

        QueryWrapper<T> wrapper = Wrappers.<T>query().likeRight(SysConstant.COLUMN_ID_TREE_PATH, parentId);
        return list(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean remove(Long id) {
        return remove(CollectionUtil.newArrayList(id));
    }

    @Override
    public boolean remove(Collection<Long> ids) {

        List<Long> parentIds = CollectionUtil.newArrayList();

        ids.forEach((id) -> {
            // 查询被删除的记录
            T entity = getById(id);
            // 获取parent_id
            Long parentId = entity.getParentId();
            parentIds.add(parentId);
        });

        boolean result = super.remove(ids);

        parentIds.forEach(parentId -> {
            if (parentId != null) {
                // 根据parent_id查询子节点数量，若为0 则恢复parent为leaf
                QueryWrapper<T> queryWrapper = Wrappers.<T>query().eq(SysConstant.COLUMN_PARENT_ID, parentId);
                long subNodeCount = getBaseMapper().selectCount(queryWrapper);
                if (subNodeCount == 0L) {
                    T parent = BeanUtils.instantiateClass(this.getEntityClass());
                    parent.setId(parentId);
                    parent.setIsLeaf(SysConstant.STATUS_ENABLED);
                    updateById(parent);
                }
            }
        });

        return result;
    }
}
