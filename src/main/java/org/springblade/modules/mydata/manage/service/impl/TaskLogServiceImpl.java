package org.springblade.modules.mydata.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.manage.entity.TaskLog;
import org.springblade.modules.mydata.manage.mapper.TaskLogMapper;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.vo.TaskLogVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 集成任务日志 服务实现类
 *
 * @author LIEN
 * @since 2022-07-18
 */
@Service
public class TaskLogServiceImpl extends BaseServiceImpl<TaskLogMapper, TaskLog> implements ITaskLogService {

    @Override
    public IPage<TaskLogVO> selectTaskLogPage(IPage<TaskLogVO> page, TaskLogVO taskLog) {
        return page.setRecords(baseMapper.selectTaskLogPage(page, taskLog));
    }

    @Override
    public IPage<TaskLog> listTaskLogPage(IPage<TaskLog> page, TaskLog taskLog) {
        Wrapper<TaskLog> queryWrapper = Wrappers.<TaskLog>lambdaQuery()
                .eq(TaskLog::getTaskId, taskLog.getTaskId())
                .orderByDesc(TaskLog::getTaskStartTime);
        return this.page(page, queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByTask(Long taskId) {
        LambdaQueryWrapper<TaskLog> queryWrapper = Wrappers.<TaskLog>lambdaQuery()
                .eq(TaskLog::getTaskId, taskId);

        return remove(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUnfinished(Date endTime) {
        LambdaQueryWrapper<TaskLog> queryWrapper = Wrappers.<TaskLog>lambdaQuery()
                .le(TaskLog::getTaskStartTime, endTime)
                .isNull(TaskLog::getTaskResult);

        remove(queryWrapper);
    }
}
