package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineTaskVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线任务 Wrapper
 *
 * @author LIEN
 * @since 2024/11/16
 */
public class PipelineTaskWrapper extends BaseWrapper<PipelineTask, PipelineTaskVO> {
    public PipelineTaskWrapper() {
    }

    public static PipelineTaskWrapper getInstance() {
        return new PipelineTaskWrapper();
    }

    @Override
    public PipelineTaskVO entityVO(PipelineTask entity) {
        return BeanUtil.copyProperties(entity, PipelineTaskVO.class);
    }
}
