package org.springblade.modules.mydata.job.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MapUtil;
import org.springblade.common.util.MdUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.data.BizDataFilter;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.job.service.JobBatchService;
import org.springblade.modules.mydata.job.service.JobDataFilterService;
import org.springblade.modules.mydata.job.service.JobDataService;
import org.springblade.modules.mydata.job.service.JobEmailService;
import org.springblade.modules.mydata.job.service.JobVarService;
import org.springblade.modules.mydata.job.util.ApiUtil;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 执行任务的线程
 *
 * @author LIEN
 * @since 2022/07/16
 */
@Slf4j
public class JobThread implements Runnable {

    private final TaskInfo taskInfo;


    public JobThread(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public void run() {
        taskInfo.appendLog("任务开始执行");

        JobDataService jobDataService = SpringUtil.getBean(JobDataService.class);
        BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
        JobExecutor jobExecutor = SpringUtil.getBean(JobExecutor.class);
        JobVarService jobVarService = SpringUtil.getBean(JobVarService.class);
        JobDataFilterService jobDataFilterService = SpringUtil.getBean(JobDataFilterService.class);
        JobBatchService jobBatchService = SpringUtil.getBean(JobBatchService.class);
        JobEmailService jobEmailService = SpringUtil.getBean(JobEmailService.class);

        taskInfo.setLastRunTime(new Date());

        // 标记本次执行是否成功
        boolean isJobSuccess = false;

        // 获取任务操作类型
        int opType = taskInfo.getOpType();

        try {
            // 解析并替换api中的环境变量
            jobVarService.parseVar(taskInfo);
            // 根据操作类型 执行读或写
            switch (opType) {
                // 提供数据
                case MdConstant.DATA_PRODUCER:
                    // 分批模式 记录上一次数据，用于对比两次数据，若重复 则结束，避免死循环
                    long lastJsonHash = -1L;
                    do {
                        // 若启用分批，则将分批参数加入请求参数中
                        if (taskInfo.isBatch()) {
                            Map<String, Object> batchParam = jobBatchService.parseToMap(taskInfo);
                            Map<String, Object> reqParams = MapUtil.union(taskInfo.getReqParams(), batchParam);
                            taskInfo.setReqParams(reqParams);
                        }

                        // 调用api 获取json
                        String json = ApiUtil.read(taskInfo);
                        // 对比上一次数据
                        if (lastJsonHash != -1L) {
                            if (lastJsonHash == HashUtil.mixHash(json)) {
                                // TODO 邮件通知用户检查任务
                                throw new RuntimeException("分批获取数据异常，最后两次获取的数据相同！");
                            }
                        }
                        lastJsonHash = HashUtil.mixHash(json);

                        // 将json按字段映射 解析为业务数据
                        jobDataService.parseData(taskInfo, json);
                        // 若没有返回数据，则结束处理
                        if (CollUtil.isEmpty(taskInfo.getProduceDataList())) {
                            break;
                        }

                        // 根据条件过滤数据
                        jobDataFilterService.doFilter(taskInfo);
                        if (CollUtil.isEmpty(taskInfo.getProduceDataList())) {
                            taskInfo.appendLog("过滤后的没有业务数据，跳过后续处理");
                            break;
                        }

                        // 保存业务数据
                        jobDataService.saveTaskData(taskInfo);

                        // 更新环境变量
                        jobVarService.saveVarValue(taskInfo, json);

                        // 递增分批参数
                        jobBatchService.incBatchParam(taskInfo);

                        // 若启用分批，则等待间隔
                        if (taskInfo.isBatch()) {
                            ThreadUtil.sleep(taskInfo.getBatchInterval(), TimeUnit.SECONDS);
                        }
                    } while (taskInfo.isBatch());

                    break;
                // 消费数据
                case MdConstant.DATA_CONSUMER:
                    String dataCode = taskInfo.getDataCode();
                    if (StrUtil.isEmpty(dataCode)) {
                        break;
                    }
                    List<BizDataFilter> filters = taskInfo.getDataFilters();
                    if (CollUtil.isNotEmpty(filters)) {
                        // 解析过滤条件值中的 自定义字符串
                        parseFilterValue(filters);
                        // 排除值为null的条件
                        filters = filters.stream().filter(filter -> filter.getValue() != null).collect(Collectors.toList());
                    }
                    int round = 0;
                    Long skip = null;
                    Integer limit = taskInfo.isBatch() ? taskInfo.getBatchSize() : null;
                    do {
                        if (taskInfo.isBatch()) {
                            skip = (long) round * taskInfo.getBatchSize();
                        }
                        // 根据过滤条件 查询数据
                        List<Map> dataList = bizDataDAO.list(MdUtil.getBizDbCode(taskInfo.getTenantId(), taskInfo.getProjectId(), taskInfo.getEnvId()), dataCode, filters, skip, limit);
                        if (CollUtil.isEmpty(dataList)) {
                            break;
                        }
                        taskInfo.setConsumeDataList(dataList);

                        // 消费模式是调用API
                        if (MdConstant.TASK_CONSUME_MODE_API == taskInfo.getConsumeMode()) {
                            // 根据字段映射转换为api参数
                            jobDataService.convertData(taskInfo);
                            // 调用api传输数据
                            ApiUtil.write(taskInfo);
                        }
                        // 消费模式是发送邮件
                        else if (MdConstant.TASK_CONSUME_MODE_EMAIL == taskInfo.getConsumeMode()) {
                            File excelFile = jobDataService.exportExcel(taskInfo);
                            excelFile = FileUtil.rename(excelFile, taskInfo.getTaskName() + "-" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN), true, true);
                            jobEmailService.sendMail(taskInfo, excelFile);
                        }

                        round++;
                        // 若启用分批，则等待间隔
                        if (taskInfo.isBatch()) {
                            ThreadUtil.sleep(taskInfo.getBatchInterval(), TimeUnit.SECONDS);
                        }
                    } while (taskInfo.isBatch());
                    break;
                default:
                    throw new RuntimeException("不支持的任务类型：" + opType);
            }

            // 任务执行成功
            isJobSuccess = true;
        } catch (Exception e) {
            taskInfo.appendLog("任务执行失败，异常：{}", e.getMessage());
            e.printStackTrace();
        } finally {
            // 恢复原生header和param，恢复变量表达式，下次可获取最新变量值
            taskInfo.setReqHeaders(taskInfo.getOriginReqHeaders());
            taskInfo.setReqParams(taskInfo.getOriginReqParams());
        }

        // 标记任务失败次数是否到达上限
        boolean isTaskFailed = false;

        if (isJobSuccess) {
            taskInfo.setExecuteResult(MdConstant.TASK_RESULT_SUCCESS);
            // 调用API成功后，重置错误次数
            taskInfo.setFailCount(0);
            // 更新任务的成功时间
            taskInfo.setLastSuccessTime(new Date());
            taskInfo.appendLog("任务执行成功");
        } else {
            taskInfo.setExecuteResult(MdConstant.TASK_RESULT_FAILED);
            // 累加失败次数
            int failCount = taskInfo.getFailCount();
            failCount++;

            // 小于失败上限，可继续执行
            if (failCount < MdConstant.TASK_MAX_FAIL_COUNT) {
                taskInfo.setFailCount(failCount);
                taskInfo.appendLog("任务第{}次失败", failCount);
            } else {
                // 标记任务异常
                isTaskFailed = true;
                taskInfo.appendLog("任务失败达到{}次，将终止且不再执行", failCount);
            }
        }

        taskInfo.setEndTime(new Date());

        jobExecutor.completeJob(taskInfo);

        if (isTaskFailed) {
            // 更新任务状态为异常
            jobExecutor.fail(taskInfo);
        } else {
            if (!MdConstant.TASK_IS_SUBSCRIBED.equals(taskInfo.getIsSubscribed())) {
                // 减少可执行次数
                int times = taskInfo.getTimes();
                // 判断可执行次数
                if (--times > 0) {
                    taskInfo.setTimes(times);
                    // 继续执行任务
                    jobExecutor.continueJob(taskInfo);
                }
//                else {
//                    jobExecutor.stopTask(taskJob.getId());
//                }
                // 执行订阅任务
                jobExecutor.executeSubscribedTask(taskInfo);
            }
        }
    }

    /**
     * 解析过滤条件中的 自定义字符串
     */
    private void parseFilterValue(List<BizDataFilter> filters) {
        if (CollUtil.isEmpty(filters)) {
            return;
        }

        filters.forEach(filter -> {
            Object value = filter.getValue();
            // 任务的最后成功时间，若没有成功过 则复用任务开始时间
            if (MdConstant.DATA_VALUE_TASK_LAST_SUCCESS_TIME.equals(value)) {
                filter.setValue(taskInfo.getLastSuccessTime());
            }
        });
    }
}