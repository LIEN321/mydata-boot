package org.springblade.modules.mydata.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.modules.mydata.manage.entity.TaskLog;
import org.springblade.modules.mydata.manage.vo.TaskLogVO;

import java.util.Date;

/**
 * 集成任务日志 服务类
 *
 * @author LIEN
 * @since 2022-07-18
 */
public interface ITaskLogService extends BaseService<TaskLog> {

    /**
     * 自定义分页
     *
     * @param page
     * @param taskLog
     * @return
     */
    IPage<TaskLogVO> selectTaskLogPage(IPage<TaskLogVO> page, TaskLogVO taskLog);

    /**
     * 查询任务日志列表
     *
     * @param page
     * @param taskLog
     * @return
     */
    IPage<TaskLog> listTaskLogPage(IPage<TaskLog> page, TaskLog taskLog);

    /**
     * 根据任务删除日志
     *
     * @param taskId 任务id
     * @return 操作结果，true-成功，false-失败
     */
    boolean deleteByTask(Long taskId);

    /**
     * 删除指定时间范围内 没有结果的日志
     *
     * @param endTime 结束时间
     */
    void deleteUnfinished(Date endTime);
}
