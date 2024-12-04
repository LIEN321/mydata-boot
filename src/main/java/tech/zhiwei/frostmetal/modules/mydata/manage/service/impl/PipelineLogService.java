package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.service.IdService;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineLogDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineLogMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineLogService;
import tech.zhiwei.tool.bean.BeanUtil;

import java.util.Date;

/**
 * 流水线执行日志 Service实现类
 *
 * @author LIEN
 * @since 2024/11/28
 */
@Service
@AllArgsConstructor
public class PipelineLogService extends IdService<PipelineLogMapper, PipelineLog> implements IPipelineLogService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long savePipelineLog(PipelineLogDTO pipelineLogDTO) {
        PipelineLog pipelineLog = BeanUtil.copyProperties(pipelineLogDTO, PipelineLog.class);
        saveOrUpdate(pipelineLog);
        return pipelineLog.getId();
    }

    @Override
    public void failLog(Long id) {
        if (id == null) {
            return;
        }

        PipelineLog pipelineLog = new PipelineLog();
        pipelineLog.setId(id);
        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
        pipelineLog.setEndTime(new Date());

        updateById(pipelineLog);
    }

    @Override
    public void stopRunningLog() {
        LambdaUpdateWrapper<PipelineLog> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(PipelineLog::getExecutionStatus, MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED)
                .set(PipelineLog::getEndTime, new Date())
                .eq(PipelineLog::getExecutionStatus, MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
        update(updateWrapper);
    }
}
