package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线 Wrapper
 *
 * @author LIEN
 * @since 2024/11/14
 */
public class PipelineWrapper extends BaseWrapper<Pipeline, PipelineVO> {
    public PipelineWrapper() {
    }

    public static PipelineWrapper getInstance() {
        return new PipelineWrapper();
    }

    @Override
    public PipelineVO entityVO(Pipeline entity) {
        return BeanUtil.copyProperties(entity, PipelineVO.class);
    }
}
