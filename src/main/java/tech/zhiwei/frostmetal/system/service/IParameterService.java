package tech.zhiwei.frostmetal.system.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.system.dto.ParameterDTO;
import tech.zhiwei.frostmetal.system.entity.SysParameter;

/**
 * 系统参数 Service接口
 *
 * @author LIEN
 * @since 2024/9/1
 */
public interface IParameterService extends IBaseService<SysParameter> {
    /**
     * 新增或更新系统参数
     *
     * @param parameterDTO 系统参数数据
     * @return id
     */
    Long saveParameter(ParameterDTO parameterDTO);
}
