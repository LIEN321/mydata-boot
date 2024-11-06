package tech.zhiwei.frostmetal.dev.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.dev.entity.DevEntity;
import tech.zhiwei.frostmetal.dev.vo.DevEntityVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 业务实体 Wrapper
 *
 * @author LIEN
 * @since 2024/9/28
 */
public class DevEntityWrapper extends BaseWrapper<DevEntity, DevEntityVO> {
    public DevEntityWrapper() {
    }

    public static DevEntityWrapper getInstance() {
        return new DevEntityWrapper();
    }

    @Override
    public DevEntityVO entityVO(DevEntity entity) {
        return BeanUtil.copyProperties(entity, DevEntityVO.class);
    }
}
