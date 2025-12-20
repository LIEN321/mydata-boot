package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.config.MydataConfiguration;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineTaskDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineTaskMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ExceptionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

import java.util.List;

/**
 * 流水线任务 Service实现类
 *
 * @author LIEN
 * @since 2024/11/16
 */
@Service
@AllArgsConstructor
public class PipelineTaskService extends BaseService<PipelineTaskMapper, PipelineTask> implements IPipelineTaskService {
    private MydataConfiguration mydataConfig;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipelineTask(PipelineTaskDTO pipelineTaskDTO) {
        PipelineTask pipelineTask = BeanUtil.copyProperties(pipelineTaskDTO, PipelineTask.class);
        saveOrUpdate(pipelineTask);
        return pipelineTask.getId();
    }

    @Override
    public List<PipelineTask> listByPipeline(Long pipelineId) {
        LambdaQueryWrapper<PipelineTask> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PipelineTask::getPipelineId, pipelineId);
        return list(queryWrapper);
    }

    @Override
    public List<PipelineTask> listEnabledByPipeline(Long pipelineId) {
        LambdaQueryWrapper<PipelineTask> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PipelineTask::getPipelineId, pipelineId);
        queryWrapper.eq(PipelineTask::getStatus, SysConstant.STATUS_ENABLED);
        return list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTasksByPipeline(Long pipelineId, List<PipelineTaskDTO> taskDTOList) {
        // 删除流水线原有的任务列表
        LambdaUpdateWrapper<PipelineTask> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(PipelineTask::getPipelineId, pipelineId);
        remove(updateWrapper);

        // 保存新的任务列表
        if (CollectionUtil.isNotEmpty(taskDTOList)) {
            List<PipelineTask> tasks = CollectionUtil.newArrayList();
            taskDTOList.forEach(taskDTO -> {
                PipelineTask task = BeanUtil.copyProperties(taskDTO, PipelineTask.class);
                int retry = ObjectUtil.defaultIfNull(taskDTO.getRetry(), 0);
                if (retry < mydataConfig.getPipelineRetryMinCount() || retry > mydataConfig.getPipelineRetryMaxCount()) {
                    throw ExceptionUtil.wrapRuntime("操作失败：重试次数不在[{}~{}]时间！", mydataConfig.getPipelineRetryMinCount(), mydataConfig.getPipelineRetryMaxCount());
                }
                task.setPipelineId(pipelineId);
                tasks.add(task);
            });
            saveBatch(tasks);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cloneByPipeline(Long sourcePipelineId, Long targetPipelineId) {
        List<PipelineTask> tasks = listByPipeline(sourcePipelineId);
        if (CollectionUtil.isEmpty(tasks)) {
            return;
        }

        List<PipelineTask> cloneTasks = CollectionUtil.newArrayList();
        tasks.forEach(task -> {
            PipelineTask cloneTask = BeanUtil.copyProperties(task, PipelineTask.class, "id", "pipelineId", "createTime", "updateTime");
            cloneTask.setPipelineId(targetPipelineId);
            cloneTasks.add(cloneTask);
        });

        saveBatch(cloneTasks);
    }
}
