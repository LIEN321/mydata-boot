package tech.zhiwei.frostmetal.modules.mydata.manage.service;

import tech.zhiwei.frostmetal.core.base.service.IIdService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineLogDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;

/**
 * 流水线执行日志 Service接口
 *
 * @author LIEN
 * @since 2024/11/28
 */
public interface IPipelineLogService extends IIdService<PipelineLog> {
    /**
     * 保存流水线执行日志
     * @param pipelineLogDTO 流水线执行日志
     * @return id
     */
    Long savePipelineLog(PipelineLogDTO pipelineLogDTO);
}
