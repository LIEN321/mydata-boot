package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineTaskDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineTaskMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

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
                task.setPipelineId(pipelineId);
                tasks.add(task);
            });
            saveBatch(tasks);
        }
    }
}
