package tech.zhiwei.frostmetal.system.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.system.entity.SysParameter;
import tech.zhiwei.frostmetal.system.vo.ParameterVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 系统参数包装类
 *
 * @author LIEN
 * @since 2024/9/1
 */
public class ParameterWrapper extends BaseWrapper<SysParameter, ParameterVO> {
    private ParameterWrapper() {
    }

    public static ParameterWrapper getInstance() {
        return new ParameterWrapper();
    }

    @Override
    public ParameterVO entityVO(SysParameter entity) {
        return BeanUtil.copyProperties(entity, ParameterVO.class);
    }
}
