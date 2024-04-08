package org.springblade.modules.mydata.job.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
            tasks.forEach(this::startTask);
        }
    }

    /**
     * 开始指定任务
     *
     * @param id 任务id
     */
    public void startTask(Long id) {
        Task task = taskService.getById(id);
        startTask(task);
    }

    /**
     * 开始指定任务
     *
     * @param task 任务对象
     */
    public void startTask(Task task) {
        if (task == null) {
            return;
        }
        // 若任务是订阅模式 则无需启动（fix #707419847）
        if (MdConstant.TASK_IS_SUBSCRIBED.equals(task.getIsSubscribed())) {
            return;
        }

        TaskInfo taskInfo = this.build(task);
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
        TaskInfo taskInfo = this.build(task);
        taskInfo.setTimes(1);
        taskInfo.setStartTime(new Date());

        taskInfo.appendLog("手动执行一次任务");
        executeJob(taskInfo);
    }

    /**
     * 停止指定任务
     *
     * @param id 任务id
     */
    public void stopTask(Long id) {
        jobCache.removeTask(id);
    }

    public void restartTask(Long id) {
        stopTask(id);
        startTask(id);
    }

    /**
     * 开始job
     *
     * @param taskInfo job
     */
    public void cacheJob(TaskInfo taskInfo) {
        taskInfo.setEndTime(null);
        // 清空已有数据
        taskInfo.setConsumeDataList(null);
        taskInfo.setProduceDataList(null);
        taskInfo.setFilteredDataList(CollUtil.toList());
        // 清空任务日志
        taskInfo.setLog(new StringBuffer());
        int i = 0;
        while (i < MdConstant.TASK_MAX_FAIL_COUNT) {
            try {
                // 设置开始时间
                taskInfo.setStartTime(new Date());

                // 计算Job的下次执行时间
                calculateNextRunTime(taskInfo);

                // 存入缓存
                jobCache.cacheJob(taskInfo);

                taskInfo.appendLog("预计执行时间：{}", DateUtil.formatDateTime(taskInfo.getNextRunTime()));
                return;
            } catch (RuntimeException e) {
                i++;
                ThreadUtil.sleep(5000);
            }
        }

        taskInfo.setExecuteResult(MdConstant.TASK_RESULT_FAILED);
        fail(taskInfo);
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
        List<Task> subTasks = taskService.listRunningSubTasks(taskInfo.getDataId());
        subTasks.forEach(task -> {
            TaskInfo subTaskInfo = build(task);
            // 订阅任务现在执行
            subTaskInfo.setStartTime(new Date());
            // 向订阅任务传入数据
            subTaskInfo.setConsumeDataList(produceDataList);
            // 指定订阅任务，调用接口发送数据
            executeJob(subTaskInfo);
        });
    }

    /**
     * 继续执行任务
     */
    public void continueJob(TaskInfo taskInfo) {
        // 执行任务
        cacheJob(taskInfo);
    }

    public void fail(TaskInfo taskInfo) {
        // 更新任务状态
        taskService.failTask(taskInfo.getId());
        // 删除任务缓存
        jobCache.removeTask(taskInfo.getId());
    }

    public void notify(String taskId) {
        TaskInfo taskInfo = jobCache.getTask(taskId);
        if (taskInfo == null) {
            log.error("notify taskJob is null, taskId = {}", taskId);
            return;
        }

        taskInfo.appendLog("缓存到期");
        executeJob(taskInfo);
    }

    public void completeJob(TaskInfo taskInfo) {
        // 更新任务的 最后执行时间、最后成功时间
        Task task = new Task();
        task.setId(taskInfo.getId());
        task.setLastRunTime(taskInfo.getLastRunTime());
        task.setLastSuccessTime(taskInfo.getLastSuccessTime());
        taskService.finishTask(task, taskInfo.getFilteredDataList());

        // 保存日志
        taskLogService.save(getTaskLog(taskInfo));
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
    private TaskInfo build(Task task) {
        TaskInfo taskInfo = new TaskInfo();

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
        Map<String, String> taskHeaders = task.getReqHeaders();
        if (CollUtil.isNotEmpty(taskHeaders)) {
            taskInfo.setReqHeaders(ObjectUtil.clone(taskHeaders));
            taskInfo.setOriginReqHeaders(taskHeaders);
        }
        // param
        Map<String, Object> taskParams = MapUtil.newHashMap();
        taskParams.putAll(task.getReqParams());
        if (CollUtil.isNotEmpty(taskParams)) {
            taskInfo.setReqParams(ObjectUtil.clone(taskParams));
            taskInfo.setOriginReqParams(taskParams);
        }
        // field var mapping
        taskInfo.setFieldVarMapping(task.getFieldVarMapping());

        // 数据过滤条件
        taskInfo.setDataFilters(jobDataFilterService.convertBizDataFilter(task.getDataFilter()));

        // 分批参数
        taskInfo.setBatch(MdConstant.ENABLED == task.getBatchStatus());
        taskInfo.setBatchInterval(task.getBatchInterval());
        taskInfo.setBatchParams(jobBatchService.parseTaskBatchParam(task.getBatchParams()));
        Integer batchSize = ObjectUtil.defaultIfNull(task.getBatchSize(), MdConstant.ROUND_DATA_COUNT);
        taskInfo.setBatchSize(batchSize);
        // 初始空的过滤数据
        taskInfo.setFilteredDataList(CollUtil.toList());
        // 消费模式
        taskInfo.setConsumeMode(task.getConsumeMode());
        // 消费推送邮箱
        taskInfo.setConsumeEmail(task.getConsumeEmail());
        // 跳过特殊情况
        taskInfo.setSkipError(task.getSkipError());

        if (task.getDataId() != null) {
            List<DataField> dataFields = dataFieldService.findByData(task.getDataId());
            // 获取配置映射的数据字段的类型
            if (CollUtil.isNotEmpty(dataFields) || CollUtil.isNotEmpty(task.getFieldMapping())) {
                // 映射 字段编号：字段类型
                Map<String, String> fieldTypeMap = dataFields.stream().collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                Map<String, String> mappingFieldType = MapUtil.newHashMap();
                task.getFieldMapping().forEach((k, v) -> {
                    mappingFieldType.put(k, fieldTypeMap.get(k));
                });

                // 映射字段的类型
                taskInfo.setMappingFieldType(mappingFieldType);
            }
        }

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
        if (taskInfo.getFailCount() > 0) {
            date = taskInfo.getNextRunTime();
        }

        CronExpression cronExpression = new CronExpression(taskInfo.getTaskPeriod());
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
            threadPoolExecutor = new ThreadPoolExecutor(
                    jobThreadCount
                    , Integer.MAX_VALUE
                    , 60L
                    , TimeUnit.SECONDS
                    , bq
                    , ThreadFactoryBuilder.create().setNamePrefix("data-task").build()
            );
        }

        return threadPoolExecutor;
    }

    private TaskLog getTaskLog(TaskInfo taskInfo) {
        TaskLog taskLog = new TaskLog();
        taskLog.setTaskId(taskInfo.getId());
        taskLog.setTaskStartTime(taskInfo.getStartTime());
        taskLog.setTaskEndTime(taskInfo.getEndTime());
        taskLog.setTaskResult(taskInfo.getExecuteResult());
        taskLog.setTaskDetail(taskInfo.getLog().toString());
        return taskLog;
    }
}
