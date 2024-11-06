package tech.zhiwei.frostmetal.dev.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.dev.dto.DevEntityDTO;
import tech.zhiwei.frostmetal.dev.dto.SaveDevEntityPropertiesDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntity;

/**
 * 业务实体 Service接口
 *
 * @author LIEN
 * @since 2024/9/28
 */
public interface IDevEntityService extends IBaseService<DevEntity> {
    /**
     * 保存业务实体
     *
     * @param devEntityDTO 业务实体
     * @return id
     */
    Long saveDevEntity(DevEntityDTO devEntityDTO);

    /**
     * 更新业务实体的继承模式和属性列表
     *
     * @param saveDevEntityPropertiesDTO 参数
     * @return 操作结果：true-成功，false-失败
     */
    void saveDevEntityProperties(SaveDevEntityPropertiesDTO saveDevEntityPropertiesDTO);
}
