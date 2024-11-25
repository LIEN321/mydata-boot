package tech.zhiwei.frostmetal.modules.mydata.manage.wrapper;

import tech.zhiwei.frostmetal.core.base.wrapper.BaseWrapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
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
    private IPipelineHistoryService pipelineHistoryService = SpringUtil.getBean(IPipelineHistoryService.class);

    public PipelineWrapper() {
    }

    public static PipelineWrapper getInstance() {
        return new PipelineWrapper();
    }

    @Override
    public PipelineVO entityVO(Pipeline entity) {
        PipelineVO pipelineVO = BeanUtil.copyProperties(entity, PipelineVO.class);

        // 获取下一次的执行时间
        pipelineVO.setNextFireTime(pipelineScheduler.getNextFireTime(entity.getId()));

        // 查询流水线的最新一次执行记录
        Long latestHistoryId = entity.getLatestHistoryId();
        if (latestHistoryId != null) {
            PipelineHistory pipelineHistory = pipelineHistoryService.getById(latestHistoryId);
            pipelineVO.setLatestHistory(PipelineHistoryWrapper.getInstance().entityVO(pipelineHistory));
        }

        return pipelineVO;
    }
}
