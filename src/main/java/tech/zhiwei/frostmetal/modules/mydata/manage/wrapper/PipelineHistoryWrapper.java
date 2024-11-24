package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineHistoryVO;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线执行记录 Wrapper
 *
 * @author LIEN
 * @since 2024/11/24
 */
public class PipelineHistoryWrapper extends BaseWrapper<PipelineHistory, PipelineHistoryVO> {
    public PipelineHistoryWrapper() {
    }

    public static PipelineHistoryWrapper getInstance() {
        return new PipelineHistoryWrapper();
    }

    @Override
    public PipelineHistoryVO entityVO(PipelineHistory entity) {
        return BeanUtil.copyProperties(entity, PipelineHistoryVO.class);
    }
}
