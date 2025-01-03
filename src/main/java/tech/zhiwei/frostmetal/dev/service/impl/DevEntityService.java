package tech.zhiwei.frostmetal.dev.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.dev.dto.DevEntityDTO;
import tech.zhiwei.frostmetal.dev.dto.SaveDevEntityPropertiesDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntity;
import tech.zhiwei.frostmetal.dev.mapper.DevEntityMapper;
import tech.zhiwei.frostmetal.dev.service.IDevEntityPropertyService;
import tech.zhiwei.frostmetal.dev.service.IDevEntityService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 业务实体 Service实现类
 *
 * @author LIEN
 * @since 2024/9/28
 */
@Service
@AllArgsConstructor
public class DevEntityService extends BaseService<DevEntityMapper, DevEntity> implements IDevEntityService {

    private IDevEntityPropertyService devEntityPropertyService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveDevEntity(DevEntityDTO devEntityDTO) {
        DevEntity devEntity = BeanUtil.copyProperties(devEntityDTO, DevEntity.class);
        saveOrUpdate(devEntity);
        return devEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDevEntityProperties(SaveDevEntityPropertiesDTO saveDevEntityPropertiesDTO) {
        Long entityId = saveDevEntityPropertiesDTO.getId();
        // 更新业务实体的继承模式
        DevEntity devEntity = new DevEntity();
        devEntity.setId(entityId);
        devEntity.setExtendMode(saveDevEntityPropertiesDTO.getExtendMode());
        updateById(devEntity);

        // 保存业务实体的属性
        devEntityPropertyService.saveDevEntityProperty(entityId, saveDevEntityPropertiesDTO.getProperties());
    }
}
