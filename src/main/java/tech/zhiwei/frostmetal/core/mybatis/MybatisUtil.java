package tech.zhiwei.frostmetal.core.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tech.zhiwei.frostmetal.core.base.common.PageParam;

/**
 * Mybatis工具类
 *
 * @author LIEN
 * @since 2024/8/26
 */
public class MybatisUtil {
    /**
     * 获取分页对象
     *
     * @param pageParam 分页参数
     * @param <T>       类型
     * @return 分页对象
     */
    public static <T> IPage<T> getPage(PageParam pageParam) {
        return new Page<>(pageParam.getCurrent(), pageParam.getPageSize());
    }
}
