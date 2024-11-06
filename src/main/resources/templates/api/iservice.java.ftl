package ${packageName}.service;

import tech.zhiwei.frostmetal.core.base.service.${parentIServiceClass};
import ${packageName}.dto.${entityClassName}DTO;
import ${packageName}.entity.${entityClassName};

/**
 * ${entityName} Service接口
 *
 * @author ${auth}
 * @since ${date}
 */
public interface I${entityClassName}Service extends ${parentIServiceClass}<${entityClassName}> {
    /**
     * 保存${entityName}
     * @param ${entityCode}DTO ${entityName}
     * @return id
     */
    Long save${entityClassName}(${entityClassName}DTO ${entityCode}DTO);
}
