package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineTaskDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;

import java.util.List;

/**
 * 流水线任务 Service接口
 *
 * @author LIEN
 * @since 2024/11/16
 */
public interface IPipelineTaskService extends IBaseService<PipelineTask> {
    /**
     * 保存流水线任务
     *
     * @param pipelineTaskDTO 流水线任务
     * @return id
     */
    Long savePipelineTask(PipelineTaskDTO pipelineTaskDTO);

    /**
     * 根据流水线 查询任务列表
     *
     * @param pipelineId 流水线id
     * @return 任务列表
     */
    List<PipelineTask> listByPipeline(Long pipelineId);
}
