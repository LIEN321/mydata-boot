package tech.zhiwei.frostmetal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.system.dto.SysApiDTO;
import tech.zhiwei.frostmetal.system.entity.SysApi;
import tech.zhiwei.frostmetal.system.mapper.SysApiMapper;
import tech.zhiwei.frostmetal.system.service.ISysApiService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;

/**
 * 系统接口 Service实现类
 *
 * @author LIEN
 * @since 2024/9/16
 */
@Service
@AllArgsConstructor
public class SysApiService extends BaseService<SysApiMapper, SysApi> implements ISysApiService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveSysApi(SysApiDTO sysApiDTO) {
        SysApi sysApi = BeanUtil.copyProperties(sysApiDTO, SysApi.class);
        saveOrUpdate(sysApi);
        return sysApi.getId();
    }

    @Override
    public SysApi findByRequest(String method, String path) {
        AssertUtil.notEmpty(method);
        AssertUtil.notEmpty(path);
        Wrapper<SysApi> queryWrapper = Wrappers.<SysApi>lambdaQuery()
            .eq(SysApi::getMethod, method)
            .and(consumer -> consumer.eq(SysApi::getPath, path).or().apply("'" + path + "' like REPLACE(path, '*', '%')"));
        return getOne(queryWrapper);
    }
}
