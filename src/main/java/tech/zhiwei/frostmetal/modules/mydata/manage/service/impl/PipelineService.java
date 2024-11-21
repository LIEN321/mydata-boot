package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.tool.bean.BeanUtil;

import java.util.List;

/**
 * 流水线 Service实现类
 *
 * @author LIEN
 * @since 2024/11/14
 */
@Service
@AllArgsConstructor
public class PipelineService extends BaseService<PipelineMapper, Pipeline> implements IPipelineService {

    private IPipelineTaskService pipelineTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipeline(PipelineDTO pipelineDTO) {
        Pipeline pipeline = BeanUtil.copyProperties(pipelineDTO, Pipeline.class);
        // 保存流水线基本信息
        saveOrUpdate(pipeline);

        // 保存编排任务列表
        pipelineTaskService.saveTasksByPipeline(pipeline.getId(), pipelineDTO.getTasks());

        return pipeline.getId();
    }

    @Override
    public List<Pipeline> listByGroup(Long groupId) {
        LambdaQueryWrapper<Pipeline> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Pipeline::getGroupId, groupId);
        return list(queryWrapper);
    }

    @Override
    public List<Pipeline> listScheduledPipelines() {
        LambdaQueryWrapper<Pipeline> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Pipeline::getIsSchedule, SysConstant.STATUS_ENABLED);
        return list(queryWrapper);
    }
}
