package ${packageName}.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.${parentServiceClass};
import ${packageName}.dto.${entityClassName}DTO;
import ${packageName}.entity.${entityClassName};
import ${packageName}.mapper.${entityClassName}Mapper;
import ${packageName}.service.I${entityClassName}Service;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * ${entityName} Service实现类
 *
 * @author ${auth}
 * @since ${date}
 */
@Service
@AllArgsConstructor
public class ${entityClassName}Service extends ${parentServiceClass}<${entityClassName}Mapper, ${entityClassName}> implements I${entityClassName}Service {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long save${entityClassName}(${entityClassName}DTO ${entityCode}DTO) {
        ${entityClassName} ${entityCode} = BeanUtil.copyProperties(${entityCode}DTO, ${entityClassName}.class);
        saveOrUpdate(${entityCode});
        return ${entityCode}.getId();
    }
}
