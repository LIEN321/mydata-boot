package tech.zhiwei.frostmetal.modules.mydata.manage.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.zhiwei.frostmetal.core.base.common.PageParam;
import tech.zhiwei.frostmetal.core.base.service.BaseService;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.dto.PipelineHistoryDTO;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineHistory;
import tech.zhiwei.frostmetal.modules.mydata.manage.mapper.PipelineHistoryMapper;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineHistoryService;
import tech.zhiwei.tool.bean.BeanUtil;

import java.util.Date;
import java.util.List;

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

    @Override
    public PipelineHistory latestHistory(Long pipelineId) {
        LambdaQueryWrapper<PipelineHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PipelineHistory::getPipelineId, pipelineId)
                .orderByDesc(PipelineHistory::getCreateTime);

        PageParam pageParam = new PageParam();
        pageParam.setCurrent(1L);
        pageParam.setPageSize(1L);
        IPage<PipelineHistory> page = page(queryWrapper, pageParam);
        List<PipelineHistory> histories = page.getRecords();
        if (CollectionUtil.isNotEmpty(histories)) {
            return histories.get(0);
        }
        return null;
    }

    @Override
    public void stopRunningHistory() {
        LambdaUpdateWrapper<PipelineHistory> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(PipelineHistory::getExecutionStatus, MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED)
                .set(PipelineHistory::getEndTime, new Date())
                .eq(PipelineHistory::getExecutionStatus, MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
        update(updateWrapper);
    }
}
