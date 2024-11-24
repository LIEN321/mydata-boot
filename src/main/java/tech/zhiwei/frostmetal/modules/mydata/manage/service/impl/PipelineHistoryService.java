package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineHistoryMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线执行记录 Service实现类
 *
 * @author LIEN
 * @since 2024/11/24
 */
@Service
@AllArgsConstructor
public class PipelineHistoryService extends BaseService<PipelineHistoryMapper, PipelineHistory> implements IPipelineHistoryService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipelineHistory(PipelineHistoryDTO pipelineHistoryDTO) {
        PipelineHistory pipelineHistory = BeanUtil.copyProperties(pipelineHistoryDTO, PipelineHistory.class);
        saveOrUpdate(pipelineHistory);
        return pipelineHistory.getId();
    }
}
