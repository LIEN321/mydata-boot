package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;

import java.util.List;

/**
 * 流水线 Service接口
 *
 * @author LIEN
 * @since 2024/11/14
 */
public interface IPipelineService extends IBaseService<Pipeline> {
    /**
     * 保存流水线
     *
     * @param pipelineDTO 流水线
     * @return id
     */
    Long savePipeline(PipelineDTO pipelineDTO);

    /**
     * 根据分组 查询流水线列表
     *
     * @param groupId 分组id
     * @return 流水线列表
     */
    List<Pipeline> listByGroup(Long groupId);

    /**
     * 查询所有启用定时的流水线
     *
     * @return 启用定时的流水线列表
     */
    List<Pipeline> listScheduledPipelines();

    /**
     * 根据项目 查询流水线列表
     *
     * @param projectId 项目id
     * @return 流水线列表
     */
    List<Pipeline> listByProject(Long projectId);

    /**
     * 克隆流水线 生成新的流水线，流水线名称后追加 -副本 字样
     *
     * @param pipelineId 源流水线id
     */
    void clonePipeline(Long pipelineId);
}
