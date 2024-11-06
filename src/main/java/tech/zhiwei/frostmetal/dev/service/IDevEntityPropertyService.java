package tech.zhiwei.frostmetal.dev.service;

import tech.zhiwei.frostmetal.core.base.service.IIdService;
import tech.zhiwei.frostmetal.dev.dto.DevEntityPropertyDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntityProperty;

import java.util.List;

/**
 * 业务实体属性 Service接口
 *
 * @author LIEN
 * @since 2024/10/07
 */
public interface IDevEntityPropertyService extends IIdService<DevEntityProperty> {
    /**
     * 保存业务实体属性
     *
     * @param entityId                 所属业务实体id
     * @param devEntityPropertyDTOList 业务实体属性列表
     */
    void saveDevEntityProperty(Long entityId, List<DevEntityPropertyDTO> devEntityPropertyDTOList);

    /**
     * 根据所属业务实体id 查询属性列表
     *
     * @param entityId 所属业务实体id
     * @return 属性列表
     */
    List<DevEntityProperty> listByEntityId(Long entityId);
}
