package tech.zhiwei.frostmetal.system.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.system.dto.ParameterDTO;
import tech.zhiwei.frostmetal.system.entity.SysParameter;
import tech.zhiwei.frostmetal.system.mapper.ParameterMapper;
import tech.zhiwei.frostmetal.system.service.IParameterService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 系统参数 Service实现类
 *
 * @author LIEN
 * @since 2024/9/1
 */
@Service
@AllArgsConstructor
public class ParameterService extends BaseService<ParameterMapper, SysParameter> implements IParameterService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveParameter(ParameterDTO parameterDTO) {
        SysParameter parameter = BeanUtil.copyProperties(parameterDTO, SysParameter.class);
        saveOrUpdate(parameter);
        return parameter.getId();
    }
}
