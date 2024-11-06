package tech.zhiwei.frostmetal.core.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;
import tech.zhiwei.frostmetal.auth.bean.AuthUser;
import tech.zhiwei.frostmetal.auth.util.AuthUtil;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.entity.BaseEntity;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.core.mybatis.MybatisUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 基础Service实现类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class BaseService<M extends BaseMapper<T>, T extends BaseEntity> extends IdService<M, T>
        implements IBaseService<T> {

    @Override
    public IPage<T> page(PageParam pageParam) {
        return page(Wrappers.emptyWrapper(), pageParam);
    }

    @Override
    public IPage<T> page(Wrapper<T> queryWrapper, PageParam pageParam) {
        IPage<T> page = MybatisUtil.getPage(pageParam);
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean remove(Long id) {
        return remove(CollectionUtil.newArrayList(id));
    }

    @Override
    public boolean remove(@NotEmpty Collection<Long> ids) {
        // 获取当前用户
        AuthUser user = AuthUtil.getUser();

        // 待处理列表
        List<T> list = CollectionUtil.newArrayList();
        // 遍历id集合
        ids.forEach((id) -> {
            // 创建实例
            T entity = BeanUtils.instantiateClass(this.getEntityClass());
            // 设置更新人
            if (user != null) {
                entity.setUpdateUser(user.getId());
                entity.setUpdateDepartment(user.getDepartmentId());
            }
            // 更新时间
            entity.setUpdateTime(DateUtil.date());
            // id
            entity.setId(id);
            // 加入列表 一并处理
            list.add(entity);
        });
        return super.updateBatchById(list) && super.removeByIds(ids);
    }

    @Override
    public boolean save(T entity) {
        handleEntity(entity);
        return super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        entityList.forEach(this::handleEntity);
        return super.saveBatch(entityList, batchSize);
    }

    @Override
    public boolean updateById(T entity) {
        this.handleEntity(entity);
        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        entityList.forEach(this::handleEntity);
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
        entityList.forEach(this::handleEntity);
        return super.saveOrUpdateBatch(entityList, batchSize);
    }

    /**
     * 处理实体的通用字段值
     *
     * @param entity
     */
    private void handleEntity(T entity) {
        if (entity == null) {
            return;
        }

        // 获取当前用户
        AuthUser user = AuthUtil.getUser();
        Long userId = null;
        Long departmentId = null;

        if (user != null) {
            userId = user.getId();
            departmentId = user.getDepartmentId();
        }

        Date now = DateUtil.date();
        // 新增逻辑，设置创建人、创建时间、业务状态、删除状态
        if (entity.getId() == null) {
            // 设置创建人
            entity.setCreateUser(userId);
            // 创建时间
            entity.setCreateTime(now);
            // 业务状态
            if (entity.getStatus() == null) {
                entity.setStatus(SysConstant.STATUS_ENABLED);
            }
            // 删除状态
            if (entity.getIsDeleted() == null) {
                entity.setIsDeleted(SysConstant.NOT_DELETED);
            }
            // 创建部门
            entity.setCreateDepartment(departmentId);
        }

        // 设置更新人
        entity.setUpdateUser(userId);
        // 更新时间
        entity.setUpdateTime(now);
        // 更新部门
        entity.setUpdateDepartment(departmentId);
    }
}
