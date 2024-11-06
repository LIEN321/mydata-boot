package tech.zhiwei.frostmetal.core.base.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import tech.zhiwei.frostmetal.core.base.entity.IdEntity;

/**
 * 最基础的Service实现类
 *
 * @author LIEN
 * @since 2024/10/9
 */
public class IdService<M extends BaseMapper<T>, T extends IdEntity> extends ServiceImpl<M, T> implements IIdService<T> {
}
