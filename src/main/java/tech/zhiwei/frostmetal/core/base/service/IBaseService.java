package tech.zhiwei.frostmetal.core.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.constraints.NotEmpty;
import tech.zhiwei.frostmetal.core.base.common.PageParam;

import java.util.Collection;

/**
 * 基础Service
 *
 * @author LIEN
 * @since 2024/8/26
 */
public interface IBaseService<T> extends IIdService<T> {
    /**
     * 无条件的分页查询
     *
     * @param pageParam 分页参数
     * @return 分页查询结果
     */
    IPage<T> page(PageParam pageParam);

    /**
     * 分页查询
     *
     * @param queryWrapper 查询条件
     * @param pageParam    分页参数
     */
    IPage<T> page(Wrapper<T> queryWrapper, PageParam pageParam);

    /**
     * 逻辑删除一条记录
     *
     * @param id 主键id
     * @return true-成功，false-失败
     */
    boolean remove(@NotEmpty Long id);

    /**
     * 逻辑删除多条记录
     *
     * @param ids 主键id集合
     * @return true-成功，false-失败
     */
    boolean remove(@NotEmpty Collection<Long> ids);
}
