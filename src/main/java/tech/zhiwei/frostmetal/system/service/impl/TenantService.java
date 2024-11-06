package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.system.dto.TenantDTO;
import tech.zhiwei.frostmetal.system.entity.Tenant;
import tech.zhiwei.frostmetal.system.mapper.TenantMapper;
import tech.zhiwei.frostmetal.system.service.ITenantService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.util.RandomUtil;

/**
 * 系统租户 Service实现类
 *
 * @author LIEN
 * @since 2024/11/02
 */
@Service
@AllArgsConstructor
public class TenantService extends BaseService<TenantMapper, Tenant> implements ITenantService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveTenant(TenantDTO tenantDTO) {
        Tenant tenant = BeanUtil.copyProperties(tenantDTO, Tenant.class);
        if (ObjectUtil.isNull(tenant.getId())) {
            // 新建租户，生成唯一的tenant_id
            tenant.setTenantId(RandomUtil.randomString(20));
        }
        saveOrUpdate(tenant);
        return tenant.getId();
    }

    @Override
    public Tenant findByCode(String code) {
        Wrapper<Tenant> queryWrapper = Wrappers.<Tenant>lambdaQuery()
                .eq(Tenant::getTenantCode, code);
        return getOne(queryWrapper);
    }
}
