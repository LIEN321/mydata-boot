package tech.zhiwei.frostmetal.core.base.service;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 最基础的Service接口
 * 继承mybatisplus的IService，方便后续扩展和代码生成
 *
 * @author LIEN
 * @since 2024/10/9
 */
public interface IIdService<T> extends IService<T> {
}
