package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineGroup;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineGroupVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线分组 Wrapper
 *
 * @author LIEN
 * @since 2024/11/13
 */
public class PipelineGroupWrapper extends BaseWrapper<PipelineGroup, PipelineGroupVO> {
    public PipelineGroupWrapper() {
    }

    public static PipelineGroupWrapper getInstance() {
        return new PipelineGroupWrapper();
    }

    @Override
    public PipelineGroupVO entityVO(PipelineGroup entity) {
        return BeanUtil.copyProperties(entity, PipelineGroupVO.class);
    }
}
