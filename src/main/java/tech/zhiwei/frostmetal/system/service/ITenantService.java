package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.system.dto.TenantDTO;
import tech.zhiwei.frostmetal.system.entity.Tenant;

/**
 * 系统租户 Service接口
 *
 * @author LIEN
 * @since 2024/11/02
 */
public interface ITenantService extends IBaseService<Tenant> {
    /**
     * 保存系统租户
     *
     * @param tenantDTO 系统租户
     * @return id
     */
    Long saveTenant(TenantDTO tenantDTO);

    /**
     * 根据编号查询租户
     *
     * @param code 租户编号
     * @return 租户
     */
    Tenant findByCode(String code);
}
