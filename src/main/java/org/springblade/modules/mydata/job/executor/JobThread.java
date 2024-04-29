package org.springblade.modules.mydata.job.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MapUtil;
import org.springblade.common.util.MdUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.data.BizDataFilter;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.job.service.*;
import org.springblade.modules.mydata.job.util.ApiUtil;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.impl.BizDataServiceImpl;
import org.springblade.modules.system.entity.UserInfo;
import org.springblade.modules.system.service.IUserService;
import org.springblade.modules.system.service.impl.UserServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 执行任务的线程
 *
 * @author LIEN
 * @since 2022/07/16
 */
@Slf4j
public class JobThread implements Runnable {
    private final JobDataService jobDataService = SpringUtil.getBean(JobDataService.class);

    private final BizDataDAO bizDataDAO = SpringUtil.getBean(BizDataDAO.class);

    private final JobExecutor jobExecutor = SpringUtil.getBean(JobExecutor.class);

    private final JobVarService jobVarService = SpringUtil.getBean(JobVarService.class);

    private final JobDataFilterService jobDataFilterService = SpringUtil.getBean(JobDataFilterService.class);

    private final JobBatchService jobBatchService = SpringUtil.getBean(JobBatchService.class);

    private final JobEmailService jobEmailService = SpringUtil.getBean(JobEmailService.class);

    private final IUserService userService = SpringUtil.getBean(UserServiceImpl.class);

    private final IBizDataService bizDataService = SpringUtil.getBean(BizDataServiceImpl.class);

    private final TaskInfo taskInfo;

    public JobThread(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    @Override
    public void run() {
        taskInfo.appendLog("任务开始执行");

        // 不是订阅任务 生成新的任务批次号，订阅任务已设置了前置任务的相同批次号
        if (!MdConstant.TASK_IS_SUBSCRIBED.equals(taskInfo.getIsSubscribed())) {
            taskInfo.setDataBatchId(RandomUtil.randomString(16));
        }
        taskInfo.appendLog("任务批次号 {}", taskInfo.getDataBatchId());

        // 设置任务最新运行时间
        taskInfo.setLastRunTime(new Date());

        // 获取任务操作类型
        int opType = taskInfo.getOpType();

        try {
            // 解析并替换api中的环境变量
            jobVarService.parseTaskVar(taskInfo);
            // 根据操作类型 执行读或写
            switch (opType) {
                // 提供数据
                case MdConstant.DATA_PRODUCER:
                    taskInfo.appendLog("获取数据开始");
                    // 分批模式 记录上一次数据，用于对比两次数据，若重复 则结束，避免死循环
                    long lastJsonHash = -1L;
                    do {
                        // 若启用分批，则将分批参数加入请求参数中
                        if (taskInfo.isBatch()) {
                            // 分批参数
                            Map<String, Object> batchParam = jobBatchService.parseToMap(taskInfo);
                            if (CollUtil.isNotEmpty(batchParam)) {
                                // 将分批参数 并入 请求参数中
                                Map<String, Object> reqParams = MapUtil.union(taskInfo.getReqParams(), batchParam);
                                taskInfo.setReqParams(reqParams);
                                taskInfo.appendLog("分批参数：{}", batchParam);
                            }
                        }

                        String json = null;
                        if (MdConstant.TASK_PRODUCE_MODE_API.equals(taskInfo.getProduceMode())) {
                            if (MdConstant.TASK_IS_SUBSCRIBED.equals(taskInfo.getIsSubscribed()) && CollUtil.isNotEmpty(taskInfo.getTaskVar())) {
                                JobVarService.parseDataFieldVar(taskInfo, taskInfo.getTaskVar());
                            }
                            // 调用api 获取json
                            taskInfo.appendLog("调用API 获取数据，method={}，url={}，headers={}，params={}", taskInfo.getApiMethod(), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams());
                            json = ApiUtil.read(taskInfo);
                            // 将json存入已接收数据，以便后续的订阅子任务复用
                            taskInfo.setAcceptedData(json);
                        } else if (MdConstant.TASK_PRODUCE_MODE_PUSH.equals(taskInfo.getProduceMode())) {
                            // 使用接收的数据
                            json = taskInfo.getAcceptedData();
                        }

                        // 校验获取的数据是否有效
                        if (StrUtil.isNullOrUndefined(json)) {
                            throw new RuntimeException("未获取有效数据，任务结束！");
                        }

                        // 对比上一次数据
                        if (lastJsonHash != -1L) {
                            if (lastJsonHash == HashUtil.mixHash(json)) {
                                // 若跳过两次数据相同报错，则任务结束，否则抛异常
                                if (MdConstant.TASK_SKIP_SAME_DATA_ERROR.equals(taskInfo.getSkipError())) {
                                    taskInfo.appendLog("跳过两次数据相同问题，正常结束");
                                    break;
                                }
                                throw new RuntimeException("分批获取数据异常，最后两次获取的数据相同！");
                            }
                        }
                        lastJsonHash = HashUtil.mixHash(json);

                        // 获取任务中的字段映射配置，若没有则跳过数据处理
                        Map<String, String> fieldMapping = taskInfo.getFieldMapping();
                        if (MapUtil.isNotEmpty(fieldMapping)) {
                            // 将json按字段映射 解析为业务数据
                            jobDataService.parseProduceData(taskInfo, json);
                            taskInfo.appendLog("获得业务数据量：{}，解析结束", taskInfo.getProduceDataList().size());
                            // 若没有返回数据，则结束处理
                            if (CollUtil.isEmpty(taskInfo.getProduceDataList())) {
                                taskInfo.appendLog("业务数据为空，任务结束");
                                break;
                            }
                        }

                        // 根据条件过滤数据
                        if (CollUtil.isNotEmpty(taskInfo.getDataFilters())) {
                            taskInfo.appendLog("过滤业务数据，过滤条件：{}", taskInfo.getDataFilters());
                            jobDataFilterService.doFilter(taskInfo);
                            taskInfo.appendLog("过滤后的剩余数据量：{}", taskInfo.getProduceDataList().size());
                        }

                        if (CollUtil.isEmpty(taskInfo.getProduceDataList())) {
                            taskInfo.appendLog("过滤后的没有业务数据，跳过保存操作");
                        } else {
                            // 保存业务数据
                            jobDataService.saveTaskData(taskInfo);
                        }

                        // 更新环境变量
                        jobVarService.saveVarValue(taskInfo, json);

                        // 递增分批参数
                        jobBatchService.incBatchParam(taskInfo);

                        // 若启用分批，则等待间隔
                        if (taskInfo.isBatch()) {
                            ThreadUtil.sleep(taskInfo.getBatchInterval(), TimeUnit.SECONDS);
                        }
                    } while (taskInfo.isBatch());

                    // 发送过滤数据通知
                    if (CollUtil.isNotEmpty(taskInfo.getFilteredDataList())) {
                        File excelFile = jobDataService.exportFilteredDataExcel(taskInfo);
                        excelFile = FileUtil.rename(excelFile, taskInfo.getTaskName() + "-" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN), true, true);
                        UserInfo userInfo = userService.userInfo(taskInfo.getCreateUser());
                        if (userInfo != null) {
                            String emailAddress = userInfo.getUser().getEmail();

                            if (StrUtil.isNotEmpty(emailAddress)) {
                                jobEmailService.sendFilteredData(taskInfo, excelFile, emailAddress);
                                taskInfo.appendLog("向邮箱{}发送过滤数据", emailAddress);
                            }
                        }
                    }

                    // 更新业务数据量，根据项目、环境、数据code 统计数据量
                    bizDataService.updateDataCount(taskInfo.getTenantId(), taskInfo.getProjectId(), taskInfo.getEnvId(), taskInfo.getDataId());

                    taskInfo.appendLog("获取数据结束，共计新增{} 更新{}", taskInfo.getInsertCount(), taskInfo.getUpdateCount());
                    break;
                // 消费数据
                case MdConstant.DATA_CONSUMER:
                    taskInfo.appendLog("消费数据开始");
                    String dataCode = taskInfo.getDataCode();
                    if (StrUtil.isEmpty(dataCode)) {
                        throw new RuntimeException("任务未关联数据项，无法消费数据");
                    }
                    // 过滤条件
                    List<BizDataFilter> filters = jobDataFilterService.parseFilterValue(taskInfo);
                    if (filters == null) {
                        filters = CollUtil.toList();
                    }

                    // 订阅任务 使用任务批次号 查询数据
                    if (MdConstant.TASK_IS_SUBSCRIBED.equals(taskInfo.getIsSubscribed())) {
                        // 构建数据批次查询条件 _MD_BATCH_ID_ = dataBatchId
                        BizDataFilter bizDataFilter = new BizDataFilter();
                        bizDataFilter.setKey(MdConstant.DATA_COLUMN_BATCH_ID);
                        bizDataFilter.setOp(MdConstant.DATA_OP_EQ);
                        bizDataFilter.setValue(taskInfo.getDataBatchId());
                        bizDataFilter.setType(MdConstant.TASK_FILTER_TYPE_VALUE);
                        filters.add(bizDataFilter);
                    }

                    // 分批次数
                    int round = 0;
                    // 查询跳过数量
                    Long skip = null;
                    // 查询最大数量
                    Integer limit = taskInfo.isBatch() ? taskInfo.getBatchSize() : null;
                    do {
                        // 若启用分批，则计算跳过数量
                        if (taskInfo.isBatch()) {
                            skip = (long) round * taskInfo.getBatchSize();
                            taskInfo.appendLog("开始执行第{}次", (round + 1));
                        }

                        // 根据过滤条件 查询数据
                        taskInfo.appendLog("查询业务数据，过滤条件是：{}，分批参数skip={} limit={}", filters, skip, limit);
                        List<Map> dataList = bizDataDAO.list(MdUtil.getBizDbCode(taskInfo.getTenantId(), taskInfo.getProjectId(), taskInfo.getEnvId()), dataCode, filters, skip, limit);
                        taskInfo.appendLog("查询业务数据的数量是 {}", dataList.size());

                        // 没有业务数据，则跳过后续处理
                        if (CollUtil.isEmpty(dataList)) {
                            taskInfo.appendLog("待消费数据为空，任务结束");
                            break;
                        }

                        // 将业务数据存入任务对象，以便后续处理
                        taskInfo.setConsumeDataList(dataList);
                        // 累加消费数据总量
                        taskInfo.setConsumeCount(taskInfo.getConsumeCount() + dataList.size());

                        // 消费模式是调用API
                        if (MdConstant.TASK_CONSUME_MODE_API.equals(taskInfo.getConsumeMode())) {
                            // 根据字段映射转换为api参数
                            jobDataService.convertConsumeData(taskInfo);

                            // 若消费任务是对象模式，则从字段映射中提取数据 替换url上的变量
                            if (MdConstant.TASK_SINGLE_MODE_OBJECT.equals(taskInfo.getDataMode())) {
                                // 记录原来的apiUrl地址
                                String originApiUrl = taskInfo.getApiUrl();
                                taskInfo.getConsumeDataList().forEach(data -> {
                                    // 从url中解析出变量 并替换值
                                    JobVarService.parseDataFieldVar(taskInfo, data);
                                    // 调用api传输数据
                                    taskInfo.appendLog("调用API 发送数据，method={}，url={}，headers={}，params={}", taskInfo.getApiMethod(), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams());
                                    ApiUtil.write(taskInfo, data);
                                    // 恢复原来的apiUrl
                                    taskInfo.setApiUrl(originApiUrl);
                                });
                            } else {
                                // 调用api传输数据
                                taskInfo.appendLog("调用API 发送数据，method={}，url={}，headers={}，params={}", taskInfo.getApiMethod(), taskInfo.getApiUrl(), taskInfo.getReqHeaders(), taskInfo.getReqParams());
                                String json = ApiUtil.write(taskInfo);
                                // 更新环境变量
                                jobVarService.saveVarValue(taskInfo, json);
                            }
                        }
                        // 消费模式是发送邮件
                        else if (MdConstant.TASK_CONSUME_MODE_EMAIL.equals(taskInfo.getConsumeMode())) {
                            File excelFile = jobDataService.exportConsumeDataExcel(taskInfo);
                            excelFile = FileUtil.rename(excelFile, taskInfo.getTaskName() + "-" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN), true, true);

                            if (StrUtil.isNotEmpty(taskInfo.getConsumeEmail())) {
                                jobEmailService.sendConsumeData(taskInfo, excelFile, taskInfo.getConsumeEmail());
                                taskInfo.appendLog("向邮箱{}发送数据", taskInfo.getConsumeEmail());
                            }
                        }

                        // 累加分批计数
                        round++;

                        // 若启用分批，则等待间隔
                        if (taskInfo.isBatch()) {
                            ThreadUtil.sleep(taskInfo.getBatchInterval(), TimeUnit.SECONDS);
                        }
                    } while (taskInfo.isBatch());

                    taskInfo.appendLog("消费数据结束，共计{}", taskInfo.getConsumeCount());
                    break;
                default:
                    throw new RuntimeException("不支持的任务类型：" + opType);
            }
            // 任务执行成功
            taskInfo.setExecuteResult(MdConstant.TASK_RESULT_SUCCESS);
            // 成功后，重置错误次数
            taskInfo.setFailCount(0);
            // 设置任务的成功时间
            taskInfo.setLastSuccessTime(new Date());
            taskInfo.appendLog("任务执行成功");

        } catch (Exception e) {
            // 任务执行失败
            taskInfo.setExecuteResult(MdConstant.TASK_RESULT_FAILED);
            // 累加失败次数
            int failCount = taskInfo.getFailCount();
            failCount++;

            // 小于失败上限，可继续执行
            if (failCount < MdConstant.TASK_MAX_FAIL_COUNT) {
                taskInfo.setFailCount(failCount);
                taskInfo.appendLog("任务第{}次失败", failCount);
            } else {
                // 任务失败，中止运行
                taskInfo.setFailed(true);
                taskInfo.appendLog("任务失败达到{}次，将终止且不再执行", failCount);

                // 发送任务失败通知邮件 给任务创建人
                UserInfo userInfo = userService.userInfo(taskInfo.getCreateUser());
                String emailAddress = userInfo.getUser().getEmail();
                jobEmailService.sendFailedNotice(taskInfo, emailAddress);
            }
            taskInfo.appendLog("任务失败原因：{}", e.getMessage());
            log.error(e.getMessage(), e);
        }

        // 设置任务结束时间
        taskInfo.setEndTime(new Date());

        jobExecutor.completeJob(taskInfo);
    }
}