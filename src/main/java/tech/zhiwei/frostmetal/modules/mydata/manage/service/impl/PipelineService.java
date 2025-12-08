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
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineVarService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.util.RandomUtil;

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
    private IPipelineVarService pipelineVarService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipeline(PipelineDTO pipelineDTO) {
        Pipeline pipeline = BeanUtil.copyProperties(pipelineDTO, Pipeline.class);
        if (pipeline.getId() == null) {
            pipeline.setWebhookCode(RandomUtil.randomString(64));
            pipeline.setDayOfWeek(new Integer[]{2, 3, 4, 5, 6});
            pipeline.setStartTime("00:00");
            pipeline.setEndTime("23:59");
            pipeline.setIntervalTime("00:15:00");
            pipeline.setIsEmail(true);
            pipeline.setEmailStrategy(new Integer[]{0});
        } else if (ObjectUtil.equals(true, pipeline.getIsSchedule()) || ObjectUtil.equals(true, pipeline.getIsWebhook())) {
            // 重新开启定时后，重置连续失败次数
            pipeline.setConsecutiveFailures(0);
        }
        // 保存流水线基本信息
        saveOrUpdate(pipeline);

        // 保存编排任务列表
        pipelineTaskService.saveTasksByPipeline(pipeline.getId(), pipelineDTO.getTasks());

        // 保存流水线变量列表
        pipelineVarService.saveVariablesByPipeline(pipeline.getId(), pipelineDTO.getVariables());

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

    @Override
    public List<Pipeline> listByProject(Long projectId) {
        LambdaQueryWrapper<Pipeline> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Pipeline::getProjectId, projectId);
        return list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clonePipeline(Long pipelineId) {
        Pipeline pipeline = getById(pipelineId);
        AssertUtil.notNull(pipeline, "复制失败：源流水线不存在！");

        // 复制新流水线
        Pipeline clonePipeline = BeanUtil.copyProperties(pipeline, Pipeline.class
                , "id", "createTime", "updateTime", "consecutiveFailures", "webhookCode", "latestHistoryId"
        );
        clonePipeline.setConsecutiveFailures(0);
        clonePipeline.setWebhookCode(RandomUtil.randomString(64));
        clonePipeline.setPipelineName(clonePipeline.getPipelineName() + " - 副本");
        clonePipeline.setIsSchedule(false);
        clonePipeline.setIsWebhook(false);

        boolean result = save(clonePipeline);
        AssertUtil.isTrue(result, "复制失败：新流水线保存失败！");

        // 复制流水线任务
        pipelineTaskService.cloneByPipeline(pipelineId, clonePipeline.getId());

        // 复制流水线变量
        pipelineVarService.cloneByPipeline(pipelineId, clonePipeline.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void enablePipeline(Long pipelineId) {
        Pipeline pipeline = getById(pipelineId);
        AssertUtil.notNull(pipeline, "操作失败：流水线不存在！");

        pipeline.setStatus(SysConstant.STATUS_ENABLED);
        updateById(pipeline);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disablePipeline(Long pipelineId) {
        Pipeline pipeline = getById(pipelineId);
        AssertUtil.notNull(pipeline, "操作失败：流水线不存在！");

        pipeline.setStatus(SysConstant.STATUS_DISABLED);
        updateById(pipeline);
    }
}
