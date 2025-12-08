package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineVarDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineVar;

import java.util.List;

/**
 * 流水线变量 Service接口
 *
 * @author LIEN
 * @since 2025/10/10
 */
public interface IPipelineVarService extends IBaseService<PipelineVar> {
    /**
     * 保存流水线变量
     *
     * @param pipelineVarDTO 流水线变量
     * @return id
     */
    Long savePipelineVar(PipelineVarDTO pipelineVarDTO);

    /**
     * 根据流水线 查询变量列表
     *
     * @param pipelineId 流水线id
     * @return 任务列表
     */
    List<PipelineVar> listByPipeline(Long pipelineId);

    /**
     * 保存流水线的变量列表
     *
     * @param pipelineId 流水线id
     * @param variables  变量列表
     */
    void saveVariablesByPipeline(Long pipelineId, List<PipelineVarDTO> variables);

    /**
     * 克隆指定流水线的任务
     *
     * @param sourcePipelineId 源流水线id
     * @param targetPipelineId 新流水线id
     */
    void cloneByPipeline(Long sourcePipelineId, Long targetPipelineId);
}
