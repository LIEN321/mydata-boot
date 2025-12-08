package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineVarDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineVar;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineVarMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineVarService;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.collection.CollectionUtil;

import java.util.List;

/**
 * 流水线变量 Service实现类
 *
 * @author LIEN
 * @since 2025/10/10
 */
@Service
@AllArgsConstructor
public class PipelineVarService extends BaseService<PipelineVarMapper, PipelineVar> implements IPipelineVarService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipelineVar(PipelineVarDTO pipelineVarDTO) {
        PipelineVar pipelineVar = BeanUtil.copyProperties(pipelineVarDTO, PipelineVar.class);
        saveOrUpdate(pipelineVar);
        return pipelineVar.getId();
    }

    @Override
    public List<PipelineVar> listByPipeline(Long pipelineId) {
        LambdaQueryWrapper<PipelineVar> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(PipelineVar::getPipelineId, pipelineId);
        return list(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveVariablesByPipeline(Long pipelineId, List<PipelineVarDTO> variables) {
        // 删除流水线原有的任务列表
        LambdaUpdateWrapper<PipelineVar> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(PipelineVar::getPipelineId, pipelineId);
        remove(updateWrapper);

        // 保存新的任务列表
        if (CollectionUtil.isNotEmpty(variables)) {
            List<PipelineVar> newVariables = CollectionUtil.newArrayList();
            variables.forEach(var -> {
                PipelineVar task = BeanUtil.copyProperties(var, PipelineVar.class);
                task.setPipelineId(pipelineId);
                newVariables.add(task);
            });
            saveBatch(newVariables);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cloneByPipeline(Long sourcePipelineId, Long targetPipelineId) {
        List<PipelineVar> vars = listByPipeline(sourcePipelineId);
        if (CollectionUtil.isEmpty(vars)) {
            return;
        }

        List<PipelineVar> cloneVars = CollectionUtil.newArrayList();
        vars.forEach(var -> {
            PipelineVar cloneVar = BeanUtil.copyProperties(var, PipelineVar.class, "id", "pipelineId", "createTime", "updateTime");
            cloneVar.setPipelineId(targetPipelineId);
            cloneVars.add(cloneVar);
        });

        saveBatch(cloneVars);
    }
}
