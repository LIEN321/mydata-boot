package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.vo.PipelineVO;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.spring.SpringUtil;

/**
 * 流水线 Wrapper
 *
 * @author LIEN
 * @since 2024/11/14
 */
public class PipelineWrapper extends BaseWrapper<Pipeline, PipelineVO> {
    private PipelineScheduler pipelineScheduler = SpringUtil.getBean(PipelineScheduler.class);

    public PipelineWrapper() {
    }

    public static PipelineWrapper getInstance() {
        return new PipelineWrapper();
    }

    @Override
    public PipelineVO entityVO(Pipeline entity) {
        PipelineVO pipelineVO = BeanUtil.copyProperties(entity, PipelineVO.class);

        pipelineVO.setNextFireTime(pipelineScheduler.getNextFireTime(entity.getId()));

        return pipelineVO;
    }
}
