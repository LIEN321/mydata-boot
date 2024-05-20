package org.springblade.modules.mydata.job.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.executor.CronExpression;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.job.bean.TaskJob;
import org.springblade.modules.mydata.job.cache.JobCache;
import org.springblade.modules.mydata.job.service.JobBatchService;
import org.springblade.modules.mydata.job.service.JobDataFilterService;
import org.springblade.modules.mydata.job.service.JobEmailService;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.entity.TaskLog;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.system.entity.UserInfo;
import org.springblade.modules.system.service.IUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 任务执行器
 *
 * @author LIEN
 * @since 2022/7/14
 */
@Slf4j
@Component
public class JobExecutor implements ApplicationRunner {

    @Resource
    @Lazy
    private ITaskService taskService;

    @Resource
    private ITaskLogService taskLogService;

    @Resource
    private JobCache jobCache;

    @Resource
    private JobBatchService jobBatchService;

    @Resource
    private JobDataFilterService jobDataFilterService;

    @Resource
    private IDataFieldService dataFieldService;

    @Resource
    private IUserService userService;

    @Resource
    private JobEmailService jobEmailService;

    /**
     * 线程池 阻塞队列
     */
    private final BlockingQueue<Runnable> bq = new LinkedBlockingQueue<>();

    /**
     * 线程池
     */
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 正在运行的任务
     */
    private final ConcurrentHashMap<Long, TaskJob> executingJobs = MapUtil.newConcurrentHashMap();

    /**
     * 线程数量
     */
    @Value("${datacenter.job.threadCount:10}")
    private int jobThreadCount;

    @Override
    public void run(ApplicationArguments args) {
        // 移除已有缓存
        jobCache.removeAll();

        // 查询已启动的任务
        List<Task> tasks = taskService.listRunningTasks();
        log.info("tasks.size() = " + tasks.size());
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(task -> {
                startTask(task, "启动服务 自动开始");
            });
        }
    }

    /**
     * 开始指定任务
     *
     * @param id 任务id
     */
    public void startTask(Long id, String starterName) {
        Task task = taskService.getById(id);
        startTask(task, starterName);
    }

    /**
     * 开始指定任务
     *
     * @param task 任务对象
     */
    public void startTask(Task task, String starterName) {
        if (task == null) {
            return;
        }
        // 若任务是订阅模式 则无需启动（fix #707419847）
        if (MdConstant.TASK_IS_SUBSCRIBED.equals(task.getIsSubscribed())) {
            return;
        }

        TaskJob taskJob = this.build(task);
        cacheJob(taskJob);
    }

    /**
     * 执行一次 指定任务
     *
     * @param id 任务id
     */
    public void executeOnce(Long id) {
        TaskJob taskJob = jobCache.getTask(id.toString());
        if (taskJob == null) {
            Task task = taskService.getById(id);
            if (task == null) {
                return;
            }
            taskJob = this.build(task);
            taskJob.setTimes(1);
        }
        executeJob(taskJob);
    }

    public void acceptData(Task task, String acceptedData) {
        if (task == null) {
            return;
        }
        TaskJob taskJob = this.build(task);
        taskJob.setTimes(1);
        taskJob.setAcceptedData(acceptedData);

        taskJob.appendLog("接收推送数据：{}", taskJob.getAcceptedData());

        executeJob(taskJob);
    }

    /**
     * 停止指定任务
     *
     * @param id 任务id
     */
    public void stopTask(Long id) {
        jobCache.removeTask(id);
        executingJobs.remove(id);
    }

    public void restartTask(Long id, String starterName) {
        stopTask(id);
        startTask(id, starterName);
    }

    /**
     * 按任务正常周期 缓存任务
     *
     * @param taskJob 任务
     */
    public void cacheJob(TaskJob taskJob) {
        cacheJob(taskJob, false);
    }

    /**
     * 按任务重试周期 缓存任务
     *
     * @param taskJob 任务
     */
    public void retryJob(TaskJob taskJob) {
        cacheJob(taskJob, true);
    }

    /**
     * 开始job
     *
     * @param taskJob job
     */
    public void cacheJob(TaskJob taskJob, boolean isRetry) {
        // 清空任务时间信息
        taskJob.setStartTime(null);
        taskJob.setNextRunTime(null);
        taskJob.setLastRunTime(null);
        taskJob.setLastSuccessTime(null);
        taskJob.setEndTime(null);

        // 恢复原来的参数，及变量表达式，以便下次可获取最新变量值
        taskJob.setReqHeaders(ObjectUtil.cloneByStream(taskJob.getOriginReqHeaders()));
        taskJob.setReqParams(ObjectUtil.cloneByStream(taskJob.getOriginReqParams()));
        taskJob.setBatchParams(ObjectUtil.cloneByStream(taskJob.getOriginBatchParams()));
        taskJob.setProduceDataList(CollUtil.toList());
        taskJob.setConsumeDataList(CollUtil.toList());
        taskJob.setFilteredDataList(CollUtil.toList());
        taskJob.setInsertCount(0);
        taskJob.setUpdateCount(0);
        taskJob.setConsumeCount(0);

        // 若不是重试任务，则清空任务日志
        if (!isRetry) {
            taskJob.setTaskLogId(null);
            taskJob.setLog(new StringBuffer());
        }
        // 重置状态
        taskJob.setExecuteResult(null);

        int i = 0;
        while (i < MdConstant.TASK_MAX_FAIL_COUNT) {
            try {
                // 设置开始时间
                taskJob.setStartTime(new Date());

                // 任务周期，若是任务重试 则使用系统默认重试间隔
                String period = isRetry ? MdConstant.TASK_FAILED_PERIOD : taskJob.getTaskPeriod();
                // 计算Job的下次执行时间
                calculateNextRunTime(taskJob, period);

                taskJob.appendLog("预计开始时间：{}，距离时长：{}秒", DateUtil.formatDateTime(taskJob.getNextRunTime()), DateUtil.between(taskJob.getStartTime(), taskJob.getNextRunTime(), DateUnit.SECOND));

                // 生成日志
                TaskLog taskLog = getTaskLog(taskJob);
                if (taskLogService.saveOrUpdate(taskLog)) {
                    taskJob.setTaskLogId(taskLog.getId());
                }

                // 存入缓存
                jobCache.cacheJob(taskJob);

                // 更新任务的下次执行时间
                taskService.updateNextRunTime(taskJob.getId(), taskJob.getNextRunTime());

                return;
            } catch (RuntimeException e) {
                i++;
                taskJob.appendLog("第{}次缓存任务出错，原因：{}", i, e.getMessage());
                ThreadUtil.sleep(1000);
            }
        }

        taskJob.setExecuteResult(MdConstant.TASK_RESULT_FAILED);
        taskJob.setFailed(true);
        taskJob.setEndTime(new Date());
        completeJob(taskJob);
    }

    /**
     * 执行订阅的子任务
     *
     * @param parentTaskJob 当前执行的任务
     */
    public void executeSubscribedTask(TaskJob parentTaskJob) {
        // 当前任务不是 提供数据，则结束
        if (MdConstant.DATA_PRODUCER != parentTaskJob.getOpType()) {
            return;
        }

        // 非数据处理的任务 不支持订阅模式
        if (ObjectUtil.isNull(parentTaskJob.getDataId())) {
            return;
        }

        // 查询相同数据的订阅任务
        List<Task> subTasks = taskService.listRunningSubTasks(parentTaskJob.getDataId(), parentTaskJob.getEnvId(), parentTaskJob.getId());
        if (CollUtil.isEmpty(subTasks)) {
            parentTaskJob.appendLog("无订阅任务", subTasks.size());
            return;
        }

        parentTaskJob.appendLog("共有{}个订阅任务", subTasks.size());

        subTasks.forEach(task -> {
            // 订阅任务 是提供数据
            if (ObjectUtil.equal(task.getOpType(), MdConstant.DATA_PRODUCER)) {
                // 调用API模式
                if (ObjectUtil.equal(task.getProduceMode(), MdConstant.TASK_PRODUCE_MODE_API)) {
                    List<Map> produceDataList = parentTaskJob.getProduceDataList();
                    // 没有业务数据 则触发子任务
                    if (CollUtil.isEmpty(produceDataList)) {
                        TaskJob subTaskJob = buildSubTaskJob(parentTaskJob, task);
                        // 执行订阅任务
                        executeJob(subTaskJob);
                        parentTaskJob.appendLog("触发执行订阅任务：{}", subTaskJob.getTaskName());
                    } else {
                        // 将业务数据作为 消费数据，逐个触发执行子任务
                        produceDataList.forEach(data -> {
                            TaskJob subTaskJob = buildSubTaskJob(parentTaskJob, task);
                            subTaskJob.setTaskVar(data);
                            // 执行订阅任务
                            executeJob(subTaskJob);
                            parentTaskJob.appendLog("触发执行订阅任务：{}", subTaskJob.getTaskName());
                        });
                    }
                }
                // 接收推送
                else {
                    acceptData(task, parentTaskJob.getAcceptedData());
                    parentTaskJob.appendLog("触发接收推送任务：{}", task.getTaskName());
                }
            }
            // 订阅任务 是消费数据
            else if (ObjectUtil.equal(task.getOpType(), MdConstant.DATA_CONSUMER)) {
                // 执行订阅任务，不判断前置任务是否有produceData，可能跨多级订阅时 再之前的任务才有数据
                TaskJob subTaskJob = buildSubTaskJob(parentTaskJob, task);
                // 执行订阅任务
                executeJob(subTaskJob);
                parentTaskJob.appendLog("触发执行订阅任务：{}", subTaskJob.getTaskName());
            }
        });
    }

    public void notify(String taskId) {
        TaskJob taskJob = jobCache.getTask(taskId);
        if (taskJob == null) {
            log.error("notify taskJob is null, taskId = {}", taskId);
            return;
        }

        executeJob(taskJob);
    }

    public void completeJob(TaskJob taskJob) {
        if (executingJobs.containsKey(taskJob.getId())) {
            // 从正在运行集合中移除
            executingJobs.remove(taskJob.getId());
        } else {
            // 任务不继续执行
            taskJob.setTimes(0);
        }

        // 任务执行次数
        int executeCount = taskJob.getExecuteCount();
        // 任务执行结果
        int executeResult = taskJob.getExecuteResult();
        // 取出任务可执行次数
        int times = taskJob.getTimes();

        // 更新任务的 最后执行时间、最后成功时间、是否异常中止
        Task task = new Task();
        task.setId(taskJob.getId());
        task.setLastRunTime(taskJob.getLastRunTime());
        task.setLastSuccessTime(taskJob.getLastSuccessTime());
        task.setNextRunTime(null);

        // 任务执行失败，且执行次数超过限制，则任务失败 并终止
        if (executeResult == MdConstant.TASK_RESULT_FAILED && executeCount >= MdConstant.TASK_MAX_FAIL_COUNT) {
            // 更新任务状态为异常
            task.setTaskStatus(MdConstant.TASK_STATUS_FAILED);

            // 记录终止
            taskJob.appendLog("任务失败达到{}次，将终止且不再执行", executeCount);
            // 删除任务缓存
            jobCache.removeTask(taskJob.getId());
            // 设置任务失败
            taskJob.setFailed(true);

            // 发送任务失败通知邮件 给任务创建人
            UserInfo userInfo = userService.userInfo(taskJob.getCreateUser());
            String emailAddress = userInfo.getUser().getEmail();
            jobEmailService.sendFailedNotice(taskJob, emailAddress);
        }

        // 更新task信息
        taskService.finishTask(task);

        // 若任务成功 则触发订阅任务，并减少可执行次数
        if (MdConstant.TASK_RESULT_SUCCESS == taskJob.getExecuteResult()) {
            // 触发订阅任务
            executeSubscribedTask(taskJob);

            // 设置任务结束时间
            taskJob.setEndTime(new Date());
            taskJob.appendLog("本次任务结束");

            // 保存日志
            taskLogService.saveOrUpdate(getTaskLog(taskJob));

            // 减少可执行次数
            times--;
            // 判断可执行次数
            if (times > 0) {
                taskJob.setTimes(times);
                // 继续执行任务
                cacheJob(taskJob);
            } else {
                // 可执行次数为0，则结束任务
                taskService.stopTask(taskJob.getId());
            }
        }
        // 任务失败
        else {
            if (!taskJob.isFailed()) {
                retryJob(taskJob);
            } else {
                // 设置任务结束时间
                taskJob.setEndTime(new Date());
                taskJob.appendLog("本次任务结束");

                // 保存日志
                taskLogService.saveOrUpdate(getTaskLog(taskJob));
            }
        }
    }

    /**
     * 更新任务日志
     *
     * @param taskJob 任务
     */
    public void updateTaskLog(TaskJob taskJob) {
        // 更新日志
        taskLogService.saveOrUpdate(getTaskLog(taskJob));
    }

    /**
     * 检测任务是否正在执行中
     *
     * @param taskId 任务id
     * @return true-执行中，false-不在执行中
     */
    public boolean isTaskExecuting(Long taskId) {
        if (ObjectUtil.isNull(taskId)) {
            return false;
        }
        return executingJobs.containsKey(taskId);
    }

    /**
     * 执行任务
     *
     * @param taskJob 任务
     */
    private void executeJob(TaskJob taskJob) {
        // 生成日志
        TaskLog taskLog = getTaskLog(taskJob);
        if (taskLogService.saveOrUpdate(taskLog)) {
            taskJob.setTaskLogId(taskLog.getId());
        }

        // 存入正在运行的任务集合中
        executingJobs.put(taskJob.getId(), taskJob);
        taskJob.setExecuteCount(taskJob.getExecuteCount() + 1);

        Runnable runnable = new JobThread(taskJob);
        getThreadPoolExecutor().execute(runnable);
    }

    /**
     * 将配置的Task 构建为可执行的TaskJob
     *
     * @param task Task
     * @return TaskJob
     */
    private TaskJob build(Task task) {
        TaskJob taskJob = new TaskJob();

        // 任务基本信息
        taskJob.setId(task.getId());
        taskJob.setTaskName(task.getTaskName());
        taskJob.setEnvId(task.getEnvId());
        taskJob.setTaskPeriod(task.getTaskPeriod());
        taskJob.setOpType(task.getOpType());
        taskJob.setDataType(task.getDataType());
        taskJob.setApiMethod(task.getApiMethod());
        taskJob.setApiUrl(task.getApiUrl());
        taskJob.setProjectId(task.getProjectId());
        taskJob.setDataMode(task.getDataMode());

        // 所属租户
        taskJob.setTenantId(task.getTenantId());
        // 字段层级前缀
        taskJob.setApiFieldPrefix(task.getApiFieldPrefix());
        // 字段映射
        taskJob.setFieldMapping(task.getFieldMapping());

        // 数据项id
        taskJob.setDataId(task.getDataId());
        // 数据项编号
        taskJob.setDataCode(task.getDataCode());

        // 唯一标识字段编号
        taskJob.setIdFieldCode(task.getIdFieldCode());

        // 是否为订阅任务
        taskJob.setIsSubscribed(task.getIsSubscribed());

        // header
        taskJob.setOriginReqHeaders(task.getReqHeaders());
        taskJob.setReqHeaders(ObjectUtil.cloneByStream(taskJob.getOriginReqHeaders()));
        // param
        Map<String, String> taskParams = task.getReqParams();
        if (CollUtil.isNotEmpty(taskParams)) {
            Map<String, Object> jobParams = MapUtil.newHashMap();
            jobParams.putAll(task.getReqParams());
            taskJob.setOriginReqParams(jobParams);
            taskJob.setReqParams(ObjectUtil.cloneByStream(taskJob.getOriginReqParams()));
        }
        // body
        taskJob.setReqBody(task.getReqBody());

        // field var mapping
        taskJob.setFieldVarMapping(task.getFieldVarMapping());

        // 数据过滤条件
        taskJob.setDataFilters(jobDataFilterService.convertBizDataFilter(task.getDataFilter()));

        // 分批参数
        taskJob.setBatch(MdConstant.ENABLED == task.getBatchStatus());
        taskJob.setBatchInterval(task.getBatchInterval());
        taskJob.setOriginBatchParams(jobBatchService.parseTaskBatchParam(task.getBatchParams()));
        taskJob.setBatchParams(ObjectUtil.cloneByStream(taskJob.getOriginBatchParams()));

        Integer batchSize = ObjectUtil.defaultIfNull(task.getBatchSize(), MdConstant.ROUND_DATA_COUNT);
        taskJob.setBatchSize(batchSize);
        // 消费模式
        taskJob.setConsumeMode(task.getConsumeMode());
        // 消费推送邮箱
        taskJob.setConsumeEmail(task.getConsumeEmail());
        // 跳过特殊情况
        taskJob.setSkipError(task.getSkipError());
        // 提供模式
        taskJob.setProduceMode(task.getProduceMode());

        if (task.getDataId() != null) {
            List<DataField> dataFields = dataFieldService.findByData(task.getDataId());
            // 获取配置映射的数据字段的类型
            if (CollUtil.isNotEmpty(dataFields) || CollUtil.isNotEmpty(task.getFieldMapping())) {
                // 映射 字段编号：字段类型
                Map<String, String> fieldTypeMapping = dataFields.stream()
                        .collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                // 映射字段的类型
                taskJob.setFieldTypeMapping(fieldTypeMapping);
            }
        }
        taskJob.setProduceDataList(CollUtil.toList());
        taskJob.setConsumeDataList(CollUtil.toList());
        taskJob.setFilteredDataList(CollUtil.toList());
        taskJob.setCreateUser(task.getCreateUser());
        Map<String, Map<String, String>> dataProcess = task.getDataProcess();
        if (dataProcess == null) {
            dataProcess = MapUtil.newHashMap();
        }
        taskJob.setDataProcess(dataProcess);

        return taskJob;
    }

    /**
     * 根据任务 构建订阅的子任务job
     *
     * @param parentTaskJob 父任务job
     * @param subTask       子任务
     * @return TaskJob 子任务job
     */
    private TaskJob buildSubTaskJob(TaskJob parentTaskJob, Task subTask) {
        // 订阅任务 是消费数据
        TaskJob subTaskJob = build(subTask);
        // 订阅任务 执行1次
        subTaskJob.setTimes(1);
        // 复用数据批次编号
        subTaskJob.setDataBatchId(parentTaskJob.getDataBatchId());
        return subTaskJob;
    }

    /**
     * 根据 任务的上次执行时间 和 设定间隔规则，计算任务的 下次执行时间
     *
     * @param taskJob 定时任务
     */
    private void calculateNextRunTime(TaskJob taskJob, String period) {
        Assert.notNull(taskJob);
        Assert.notEmpty(taskJob.getTaskPeriod());

        Date date = taskJob.getStartTime();
        CronExpression cronExpression = new CronExpression(period);
        Date nextRunTime = cronExpression.getNextValidTimeAfter(date);
        taskJob.setNextRunTime(nextRunTime);
    }

    /**
     * 从线程池获取执行器
     *
     * @return ThreadPoolExecutor
     */
    private ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(jobThreadCount, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, bq, ThreadFactoryBuilder.create()
                    .setNamePrefix("data-task")
                    .build());
        }

        return threadPoolExecutor;
    }

    private TaskLog getTaskLog(TaskJob taskJob) {
        TaskLog taskLog = new TaskLog();
        taskLog.setId(taskJob.getTaskLogId());
        taskLog.setTaskId(taskJob.getId());
        taskLog.setTaskStartTime(taskJob.getStartTime());
        taskLog.setTaskEndTime(taskJob.getEndTime());
        taskLog.setTaskResult(taskJob.getExecuteResult());
        taskLog.setTaskDetail(taskJob.getLog().toString());
        return taskLog;
    }
}
