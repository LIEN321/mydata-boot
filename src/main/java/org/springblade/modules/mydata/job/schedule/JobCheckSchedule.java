package org.springblade.modules.mydata.job.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.job.cache.JobCache;
import org.springblade.modules.mydata.job.executor.JobExecutor;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 检测任务存活的定时任务
 *
 * @author LIEN
 * @since 2023/11/15
 */
@Slf4j
@Component
public class JobCheckSchedule {

    @Resource
    private ITaskService taskService;

    @Resource
    private JobCache jobCache;

    @Resource
    private JobExecutor jobExecutor;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void jobCheck() {
        // 查询运行中的任务记录列表
        List<Task> tasks = taskService.listRunningTasks();
        if (CollUtil.isEmpty(tasks)) {
            return;
        }

        // 逐个检测是否存在对应缓存，若无则重新生成缓存任务
        tasks.forEach(task -> {
            String taskId = StrUtil.toString(task.getId());
            // 先检查task缓存
            TaskInfo taskInfo = jobCache.getTask(taskId);
            if (taskInfo == null) {
                jobExecutor.startTask(task);
                log.info("修复Task id:{} name:{}", task.getId(), task.getTaskName());
            }
            // 再检查task的job缓存
            else if (jobCache.getJob(taskId) == null) {
                jobExecutor.startTask(task);
                log.info("修复Job id:{} name:{}", task.getId(), task.getTaskName());
            }
        });
    }
}
