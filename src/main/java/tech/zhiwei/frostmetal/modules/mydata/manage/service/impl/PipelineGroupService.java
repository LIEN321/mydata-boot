package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineGroupDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineGroup;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineGroupMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineGroupService;
import tech.zhiwei.tool.bean.BeanUtil;

/**
 * 流水线分组 Service实现类
 *
 * @author LIEN
 * @since 2024/11/13
 */
@Service
@AllArgsConstructor
public class PipelineGroupService extends BaseService<PipelineGroupMapper, PipelineGroup> implements IPipelineGroupService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipelineGroup(PipelineGroupDTO pipelineGroupDTO) {
        PipelineGroup pipelineGroup = BeanUtil.copyProperties(pipelineGroupDTO, PipelineGroup.class);
        saveOrUpdate(pipelineGroup);
        return pipelineGroup.getId();
    }
}
