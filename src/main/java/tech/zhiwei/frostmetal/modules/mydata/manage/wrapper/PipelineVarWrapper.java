package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineVar;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineVarVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线变量 Wrapper
 *
 * @author LIEN
 * @since 2025/10/10
 */
public class PipelineVarWrapper extends BaseWrapper<PipelineVar, PipelineVarVO> {
    public PipelineVarWrapper() {
    }

    public static PipelineVarWrapper getInstance() {
        return new PipelineVarWrapper();
    }

    @Override
    public PipelineVarVO entityVO(PipelineVar entity) {
        return BeanUtil.copyProperties(entity, PipelineVarVO.class);
    }
}
