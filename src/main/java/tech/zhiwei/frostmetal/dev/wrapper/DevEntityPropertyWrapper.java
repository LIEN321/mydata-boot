package tech.zhiwei.frostmetal.dev.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.dev.entity.DevEntityProperty;
import tech.zhiwei.frostmetal.dev.vo.DevEntityPropertyVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 业务实体属性 Wrapper
 *
 * @author LIEN
 * @since 2024/10/8
 */
public class DevEntityPropertyWrapper extends BaseWrapper<DevEntityProperty, DevEntityPropertyVO> {
    public DevEntityPropertyWrapper() {
    }

    public static DevEntityPropertyWrapper getInstance() {
        return new DevEntityPropertyWrapper();
    }

    @Override
    public DevEntityPropertyVO entityVO(DevEntityProperty entity) {
        return BeanUtil.copyProperties(entity, DevEntityPropertyVO.class);
    }
}
