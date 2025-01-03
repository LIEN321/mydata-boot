package tech.zhiwei.frostmetal.core.base.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础实体对象包装类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public abstract class BaseWrapper<E, V> {

    /**
     * 实体 转 VO
     *
     * @param entity 实体对象
     * @return VO对象
     */
    public abstract V entityVO(E entity);

    /**
     * 实体列表 转 VO列表
     *
     * @param entities 实体列表
     * @return VO列表
     */
    public List<V> listVO(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::entityVO).collect(Collectors.toList());
    }

    /**
     * 分页实体列表 转 分页VO列表
     *
     * @param entityPage 分页实体列表
     * @return 分页VO列表
     */
    public IPage<V> pageVO(IPage<E> entityPage) {
        List<V> records = this.listVO(entityPage.getRecords());
        IPage<V> pageVO = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        pageVO.setRecords(records);
        return pageVO;
    }
}
