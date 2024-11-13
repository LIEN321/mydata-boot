package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IBaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineGroupDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineGroup;

/**
 * 流水线分组 Service接口
 *
 * @author LIEN
 * @since 2024/11/13
 */
public interface IPipelineGroupService extends IBaseService<PipelineGroup> {
    /**
     * 保存流水线分组
     * @param pipelineGroupDTO 流水线分组
     * @return id
     */
    Long savePipelineGroup(PipelineGroupDTO pipelineGroupDTO);
}
