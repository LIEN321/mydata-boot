package org.springblade.modules.mydata.job.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.executor.CronExpression;
import org.springblade.common.constant.MdConstant;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.job.cache.JobCache;
import org.springblade.modules.mydata.job.service.JobBatchService;
import org.springblade.modules.mydata.job.service.JobDataFilterService;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.entity.TaskLog;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.service.ITaskService;
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
    private final ConcurrentHashMap<Long, TaskInfo> executingJobs = MapUtil.newConcurrentHashMap();

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

        TaskInfo taskInfo = this.build(task, starterName);
        cacheJob(taskInfo);
    }

    /**
     * 执行一次 指定任务
     *
     * @param id 任务id
     */
    public void executeOnce(Long id) {
        Task task = taskService.getById(id);
        if (task == null) {
            return;
        }
        TaskInfo taskInfo = this.build(task, "手动执行");
        taskInfo.setTimes(1);
        taskInfo.setStartTime(new Date());
        taskInfo.appendLog("任务开始执行，触发功能是 {}", taskInfo.getStarterName());
        // 生成日志
        TaskLog taskLog = getTaskLog(taskInfo);
        if (taskLogService.saveOrUpdate(taskLog)) {
            taskInfo.setTaskLogId(taskLog.getId());
        }

        executeJob(taskInfo);
    }

    public void acceptData(Task task, String acceptedData) {
        if (task == null) {
            return;
        }
        TaskInfo taskInfo = this.build(task, "接收推送数据");
        taskInfo.setTimes(1);
        taskInfo.setStartTime(new Date());
        taskInfo.setAcceptedData(acceptedData);

        taskInfo.appendLog("任务开始执行，触发功能是 {}", taskInfo.getStarterName());
        taskInfo.appendLog("接收推送数据：{}", taskInfo.getAcceptedData());

        // 生成日志
        TaskLog taskLog = getTaskLog(taskInfo);
        if (taskLogService.saveOrUpdate(taskLog)) {
            taskInfo.setTaskLogId(taskLog.getId());
        }

        executeJob(taskInfo);
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
     * 开始job
     *
     * @param taskInfo job
     */
    public void cacheJob(TaskInfo taskInfo) {
        // 清空任务时间信息
        taskInfo.setStartTime(null);
        taskInfo.setNextRunTime(null);
        taskInfo.setLastRunTime(null);
        taskInfo.setLastSuccessTime(null);
        taskInfo.setEndTime(null);

        // 恢复原来的参数，及变量表达式，以便下次可获取最新变量值
        taskInfo.setReqHeaders(ObjectUtil.cloneByStream(taskInfo.getOriginReqHeaders()));
        taskInfo.setReqParams(ObjectUtil.cloneByStream(taskInfo.getOriginReqParams()));
        taskInfo.setBatchParams(ObjectUtil.cloneByStream(taskInfo.getOriginBatchParams()));
        taskInfo.setProduceDataList(CollUtil.toList());
        taskInfo.setConsumeDataList(CollUtil.toList());
        taskInfo.setFilteredDataList(CollUtil.toList());
        taskInfo.setInsertCount(0);
        taskInfo.setUpdateCount(0);

        // 清空任务日志
        taskInfo.setLog(new StringBuffer());
        taskInfo.setTaskLogId(null);
        // 重置状态
        taskInfo.setExecuteResult(null);

        taskInfo.appendLog("任务开始执行，触发功能是 {}", taskInfo.getStarterName());

        int i = 0;
        while (i < MdConstant.TASK_MAX_FAIL_COUNT) {
            try {
                // 设置开始时间
                taskInfo.setStartTime(new Date());

                // 计算Job的下次执行时间
                calculateNextRunTime(taskInfo);

                taskInfo.appendLog("预计执行时间：{}，缓存时长：{}秒"
                        , DateUtil.formatDateTime(taskInfo.getNextRunTime())
                        , DateUtil.between(taskInfo.getStartTime(), taskInfo.getNextRunTime(), DateUnit.SECOND));

                // 生成日志
                TaskLog taskLog = getTaskLog(taskInfo);
                if (taskLogService.saveOrUpdate(taskLog)) {
                    taskInfo.setTaskLogId(taskLog.getId());
                }

                // 存入缓存
                jobCache.cacheJob(taskInfo);

                // 更新任务的下次执行时间
                taskService.updateNextRunTime(taskInfo.getId(), taskInfo.getNextRunTime());

                return;
            } catch (RuntimeException e) {
                i++;
                taskInfo.appendLog("第{}次缓存任务出错，原因：{}", i, e.getMessage());
                ThreadUtil.sleep(5000);
            }
        }

        taskInfo.setExecuteResult(MdConstant.TASK_RESULT_FAILED);
        taskInfo.setFailed(true);
        taskInfo.setEndTime(new Date());
        completeJob(taskInfo);
    }

    /**
     * 执行订阅任务
     *
     * @param taskInfo 当前执行的任务
     */
    public void executeSubscribedTask(TaskInfo taskInfo) {
        // 当前任务不是 提供数据，则结束
        if (MdConstant.DATA_PRODUCER != taskInfo.getOpType()) {
            return;
        }

        List<Map> produceDataList = taskInfo.getProduceDataList();
        if (CollUtil.isEmpty(produceDataList)) {
            return;
        }
        // 查询相同数据的订阅任务
        List<Task> subTasks = taskService.listRunningSubTasks(taskInfo.getDataId(), taskInfo.getEnvId(), taskInfo.getId());
        subTasks.forEach(task -> {
            TaskInfo subTaskInfo = build(task, StrUtil.format("{} 触发执行当前订阅任务", taskInfo.getTaskName()));
            // 订阅任务现在执行
            subTaskInfo.setStartTime(new Date());
            // 设置数据批次编号
            subTaskInfo.setDataBatchId(taskInfo.getDataBatchId());
            // 生成日志
            TaskLog taskLog = getTaskLog(subTaskInfo);
            if (taskLogService.saveOrUpdate(taskLog)) {
                taskInfo.setTaskLogId(taskLog.getId());
            }
            // 指定订阅任务，调用接口发送数据
            executeJob(subTaskInfo);
        });
    }

    public void notify(String taskId) {
        TaskInfo taskInfo = jobCache.getTask(taskId);
        if (taskInfo == null) {
            log.error("notify taskJob is null, taskId = {}", taskId);
            return;
        }

        // 存入正在运行的任务集合中
        executingJobs.put(taskInfo.getId(), taskInfo);

        taskInfo.appendLog("缓存到期");
        executeJob(taskInfo);
    }

    public void completeJob(TaskInfo taskInfo) {
        // 从正在运行集合中移除
        executingJobs.remove(taskInfo.getId());

        // 更新任务的 最后执行时间、最后成功时间
        Task task = new Task();
        task.setId(taskInfo.getId());
        task.setLastRunTime(taskInfo.getLastRunTime());
        task.setLastSuccessTime(taskInfo.getLastSuccessTime());
        task.setNextRunTime(null);

        // 若任务异常
        if (taskInfo.isFailed()) {
            // 更新任务状态为异常
            task.setTaskStatus(MdConstant.TASK_STATUS_FAILED);
            // 删除任务缓存
            jobCache.removeTask(taskInfo.getId());
            // 清空可执行次数
            taskInfo.setTimes(0);
        } else {
            // 任务未异常
            if (MdConstant.TASK_IS_SUBSCRIBED.equals(taskInfo.getIsSubscribed())) {
                // 是订阅任务 且未成功，则继续执行
                if (taskInfo.getFailCount() > 0) {
                    taskInfo.setTaskPeriod(MdConstant.TASK_FAILED_PERIOD);
                } else {
                    // 订阅任务 成功，则结束
                    taskInfo.setTimes(0);
                }
            } else {
                // 不是订阅任务，则触发订阅任务
                executeSubscribedTask(taskInfo);
            }
        }

        // 更新task信息
        taskService.finishTask(task);

        // 保存日志
        taskLogService.saveOrUpdate(getTaskLog(taskInfo));

        // 减少可执行次数
        int times = taskInfo.getTimes();
        // 判断可执行次数
        if (--times > 0) {
            taskInfo.setTimes(times);
            // 继续执行任务
            cacheJob(taskInfo);
        }
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
     * @param taskInfo 任务
     */
    private void executeJob(TaskInfo taskInfo) {
        taskInfo.appendLog("任务存入执行队列");
        Runnable runnable = new JobThread(taskInfo);
        getThreadPoolExecutor().execute(runnable);
    }

    /**
     * 将配置的Task 构建为可执行的TaskJob
     *
     * @param task Task
     * @return TaskJob
     */
    private TaskInfo build(Task task, String starterName) {
        TaskInfo taskInfo = new TaskInfo();

        taskInfo.setStarterName(starterName);
        // 任务基本信息
        taskInfo.setId(task.getId());
        taskInfo.setTaskName(task.getTaskName());
        taskInfo.setEnvId(task.getEnvId());
        taskInfo.setTaskPeriod(task.getTaskPeriod());
        taskInfo.setOpType(task.getOpType());
        taskInfo.setDataType(task.getDataType());
        taskInfo.setApiMethod(task.getApiMethod());
        taskInfo.setApiUrl(task.getApiUrl());
        taskInfo.setProjectId(task.getProjectId());
        taskInfo.setDataMode(task.getDataMode());

        // 所属租户
        taskInfo.setTenantId(task.getTenantId());
        // 字段层级前缀
        taskInfo.setApiFieldPrefix(task.getApiFieldPrefix());
        // 字段映射
        taskInfo.setFieldMapping(task.getFieldMapping());

        // 数据项id
        taskInfo.setDataId(task.getDataId());
        // 数据项编号
        taskInfo.setDataCode(task.getDataCode());

        // 唯一标识字段编号
        taskInfo.setIdFieldCode(task.getIdFieldCode());

        // 是否为订阅任务
        taskInfo.setIsSubscribed(task.getIsSubscribed());

        // header
        taskInfo.setOriginReqHeaders(task.getReqHeaders());
        taskInfo.setReqHeaders(ObjectUtil.cloneByStream(taskInfo.getOriginReqHeaders()));
        // param
        Map<String, String> taskParams = task.getReqParams();
        if (CollUtil.isNotEmpty(taskParams)) {
            Map<String, Object> jobParams = MapUtil.newHashMap();
            jobParams.putAll(task.getReqParams());
            taskInfo.setOriginReqParams(jobParams);
            taskInfo.setReqParams(ObjectUtil.cloneByStream(taskInfo.getOriginReqParams()));
        }
        // field var mapping
        taskInfo.setFieldVarMapping(task.getFieldVarMapping());

        // 数据过滤条件
        taskInfo.setDataFilters(jobDataFilterService.convertBizDataFilter(task.getDataFilter()));

        // 分批参数
        taskInfo.setBatch(MdConstant.ENABLED == task.getBatchStatus());
        taskInfo.setBatchInterval(task.getBatchInterval());
        taskInfo.setOriginBatchParams(jobBatchService.parseTaskBatchParam(task.getBatchParams()));
        taskInfo.setBatchParams(ObjectUtil.cloneByStream(taskInfo.getOriginBatchParams()));

        Integer batchSize = ObjectUtil.defaultIfNull(task.getBatchSize(), MdConstant.ROUND_DATA_COUNT);
        taskInfo.setBatchSize(batchSize);
        // 消费模式
        taskInfo.setConsumeMode(task.getConsumeMode());
        // 消费推送邮箱
        taskInfo.setConsumeEmail(task.getConsumeEmail());
        // 跳过特殊情况
        taskInfo.setSkipError(task.getSkipError());
        // 提供模式
        taskInfo.setProduceMode(task.getProduceMode());

        if (task.getDataId() != null) {
            List<DataField> dataFields = dataFieldService.findByData(task.getDataId());
            // 获取配置映射的数据字段的类型
            if (CollUtil.isNotEmpty(dataFields) || CollUtil.isNotEmpty(task.getFieldMapping())) {
                // 映射 字段编号：字段类型
                Map<String, String> fieldTypeMap = dataFields.stream()
                        .collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                Map<String, String> fieldTypeMapping = MapUtil.newHashMap();
                task.getFieldMapping().forEach((k, v) -> {
                    fieldTypeMapping.put(k, fieldTypeMap.get(k));
                });

                // 映射字段的类型
                taskInfo.setFieldTypeMapping(fieldTypeMapping);
            }
        }
        taskInfo.setProduceDataList(CollUtil.toList());
        taskInfo.setConsumeDataList(CollUtil.toList());
        taskInfo.setFilteredDataList(CollUtil.toList());
        taskInfo.setCreateUser(task.getCreateUser());

        return taskInfo;
    }

    /**
     * 根据 任务的上次执行时间 和 设定间隔规则，计算任务的 下次执行时间
     *
     * @param taskInfo 定时任务
     */
    private void calculateNextRunTime(TaskInfo taskInfo) {
        Assert.notNull(taskInfo);
        Assert.notEmpty(taskInfo.getTaskPeriod());

        Date date = taskInfo.getStartTime();
        String period = taskInfo.getTaskPeriod();
        if (taskInfo.getFailCount() > 0) {
            //            date = taskInfo.getNextRunTime();
            period = MdConstant.TASK_FAILED_PERIOD;
        }

        CronExpression cronExpression = new CronExpression(period);
        Date nextRunTime = cronExpression.getNextValidTimeAfter(date);
        taskInfo.setNextRunTime(nextRunTime);
    }

    /**
     * 从线程池获取执行器
     *
     * @return
     */
    private ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(jobThreadCount, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, bq, ThreadFactoryBuilder.create()
                    .setNamePrefix("data-task")
                    .build());
        }

        return threadPoolExecutor;
    }

    private TaskLog getTaskLog(TaskInfo taskInfo) {
        TaskLog taskLog = new TaskLog();
        taskLog.setId(taskInfo.getTaskLogId());
        taskLog.setTaskId(taskInfo.getId());
        taskLog.setTaskStartTime(taskInfo.getStartTime());
        taskLog.setTaskEndTime(taskInfo.getEndTime());
        taskLog.setTaskResult(taskInfo.getExecuteResult());
        taskLog.setTaskDetail(taskInfo.getLog().toString());
        return taskLog;
    }
}
