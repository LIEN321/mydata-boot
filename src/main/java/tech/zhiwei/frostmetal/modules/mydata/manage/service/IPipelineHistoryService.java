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
     * @param pipelineHistoryDTO 流水线执行记录
     * @return id
     */
    Long savePipelineHistory(PipelineHistoryDTO pipelineHistoryDTO);
}
