package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.vo.TenantVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 系统租户 Wrapper
 *
 * @author LIEN
 * @since 2024/11/02
 */
public class TenantWrapper extends BaseWrapper<Tenant, TenantVO> {
    public TenantWrapper() {
    }

    public static TenantWrapper getInstance() {
        return new TenantWrapper();
    }

    @Override
    public TenantVO entityVO(Tenant entity) {
        return BeanUtil.copyProperties(entity, TenantVO.class);
    }
}
