package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yomahub.liteflow.builder.el.ELBus;
import com.yomahub.liteflow.builder.el.ELWrapper;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.constant.LiteFlowConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineVarService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.util.ArrayUtil;
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

        buildLiteFlowEl(pipeline);

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

    /**
     * 为流水线构造LiteFlow的EL表达式
     *
     * @param pipeline 流水线
     */
    private void buildLiteFlowEl(Pipeline pipeline) {
        Long pipelineId = pipeline.getId();

        // 查询最新的任务列表
        List<PipelineTask> tasks = pipelineTaskService.listByPipeline(pipelineId);

        if (CollectionUtil.isNotEmpty(tasks)) {
            List<ELWrapper> elWrappers = CollectionUtil.newArrayList();
            // 遍历所有任务
            for (PipelineTask task : tasks) {
                // 根据任务类型、任务id 构建EL的节点
                elWrappers.add(ELBus.element(task.getTaskType())
                        .bind(LiteFlowConstant.BIND_KEY_TASK_ID, task.getId().toString())
                );
            }

            // TODO 暂时用串联模式，后续根据前端一起调整为复杂模式
            // 将所有节点生成 串联 的EL
            if (CollectionUtil.isNotEmpty(elWrappers)) {
                String liteflowEl = ELBus.then(ArrayUtil.toArray(elWrappers, ELWrapper.class)).toEL();
                // 校验EL是否正确
                boolean isValid = LiteFlowChainELBuilder.validate(liteflowEl);
                AssertUtil.isTrue(isValid, "校验失败：配置的任务无法执行（LiteFLow），请重试或反馈问题！");

                // 保存EL
                pipeline.setLiteflowEl(liteflowEl);
                updateById(pipeline);
            }
        }
    }
}
