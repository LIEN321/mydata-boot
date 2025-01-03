package ${packageName}.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import ${packageName}.entity.${entityClassName};
import ${packageName}.vo.${entityClassName}VO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * ${entityName} Wrapper
 *
 * @author ${auth}
 * @since ${date}
 */
public class ${entityClassName}Wrapper extends BaseWrapper<${entityClassName}, ${entityClassName}VO> {
    public ${entityClassName}Wrapper() {
    }

    public static ${entityClassName}Wrapper getInstance() {
        return new ${entityClassName}Wrapper();
    }

    @Override
    public ${entityClassName}VO entityVO(${entityClassName} entity) {
        return BeanUtil.copyProperties(entity, ${entityClassName}VO.class);
    }
}
