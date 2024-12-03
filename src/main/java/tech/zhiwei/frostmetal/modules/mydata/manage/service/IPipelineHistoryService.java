package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;

/**
 * 流水线执行记录 Service接口
 *
 * @author LIEN
 * @since 2024/11/24
 */
public interface IPipelineHistoryService extends IBaseService<PipelineHistory> {
    /**
     * 保存流水线执行记录
     *
     * @param pipelineHistoryDTO 流水线执行记录
     * @return id
     */
    Long savePipelineHistory(PipelineHistoryDTO pipelineHistoryDTO);

    /**
     * 查询流水线的最新一次执行记录
     *
     * @param pipelineId 流水线id
     * @return 最新一次执行记录
     */
    PipelineHistory latestHistory(Long pipelineId);

    /**
     * 中止运行中的历史记录
     */
    void stopRunningHistory();
}
