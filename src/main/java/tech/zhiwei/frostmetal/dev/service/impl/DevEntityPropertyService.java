package tech.zhiwei.frostmetal.dev.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.IdService;
import tech.zhiwei.frostmetal.dev.dto.DevEntityPropertyDTO;
import tech.zhiwei.frostmetal.dev.entity.DevEntityProperty;
import tech.zhiwei.frostmetal.dev.mapper.DevEntityPropertyMapper;
import tech.zhiwei.frostmetal.dev.service.IDevEntityPropertyService;
import tech.zhiwei.tool.bean.BeanUtil;

import java.util.List;

/**
 * 业务实体属性 Service实现类
 *
 * @author LIEN
 * @since 2024/10/07
 */
@Service
@AllArgsConstructor
public class DevEntityPropertyService extends IdService<DevEntityPropertyMapper, DevEntityProperty> implements IDevEntityPropertyService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveDevEntityProperty(Long entityId, List<DevEntityPropertyDTO> devEntityPropertyDTOList) {
        // 删除原有属性
        remove(Wrappers.<DevEntityProperty>lambdaUpdate().eq(DevEntityProperty::getEntityId, entityId));

        // 保存新的属性列表
        List<DevEntityProperty> devEntityPropertyList = BeanUtil.copyToList(devEntityPropertyDTOList, DevEntityProperty.class);
        devEntityPropertyList.forEach(p -> {
            p.setEntityId(entityId);
            p.setId(null);
        });

        saveBatch(devEntityPropertyList);
    }

    @Override
    public List<DevEntityProperty> listByEntityId(Long entityId) {
        Wrapper<DevEntityProperty> queryWrapper = Wrappers.<DevEntityProperty>lambdaQuery()
                .eq(DevEntityProperty::getEntityId, entityId);
        return list(queryWrapper);
    }
}
