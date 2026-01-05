package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import cn.hutool.core.date.DateUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.core.constant.SysConstant;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineLogService;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineTaskService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.SendDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.email.SendEmail;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline.StopPipeline;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline.TriggerPipeline;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.FilterData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseDataToJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseJsonToData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ProcessData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.WriteDataToExcel;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.script.JsScript;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var.ParseJsonToVar;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var.SetPipelineVar;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.QueryDataFromWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.RemoveData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.SaveDataToWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.webhook.GetJsonFromWebhook;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.ArrayUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水线任务执行器
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Getter
@Slf4j
@NoArgsConstructor
public abstract class TaskExecutor {
    private PipelineTask pipelineTask;
    private PipelineLog pipelineLog;
    private final IDataFieldService dataFieldService = SpringUtil.getBean(IDataFieldService.class);
    private final IPipelineTaskService taskService = SpringUtil.getBean(IPipelineTaskService.class);
    private final IPipelineLogService pipelineLogService = SpringUtil.getBean(IPipelineLogService.class);

    // protected TaskExecutor(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     this.pipelineTask = pipelineTask;
    //     this.pipelineLog = pipelineLog;
    // }

    // 工厂方法
    public static TaskExecutor create(PipelineTask task) {
        return switch (task.getTaskType()) {
            // 从API获取JSON
            case MyDataConstant.TASK_TYPE_API_GET_JSON -> new GetJsonFromApi();
            // 向API发送数据
            case MyDataConstant.TASK_TYPE_API_SEND_DATA -> new SendDataToApi();
            // 从Webhook接收JSON
            case MyDataConstant.TASK_TYPE_WEBHOOK_GET_JSON -> new GetJsonFromWebhook();
            // 用Webhook触发流水线
            case MyDataConstant.TASK_TYPE_TRIGGER_PIPELINE -> new TriggerPipeline();
            // 用Webhook触发流水线
            case MyDataConstant.TASK_TYPE_STOP_PIPELINE -> new StopPipeline();
            // JSON转数据
            case MyDataConstant.TASK_TYPE_JSON_TO_DATA -> new ParseJsonToData();
            // 数据转JSON
            case MyDataConstant.TASK_TYPE_DATA_TO_JSON -> new ParseDataToJson();
            // 过滤数据
            case MyDataConstant.TASK_TYPE_FILTER_DATA -> new FilterData();
            // 处理数据
            case MyDataConstant.TASK_TYPE_PROCESS_DATA -> new ProcessData();
            // 数据写入Excel
            case MyDataConstant.TASK_TYPE_WRITE_EXCEL -> new WriteDataToExcel();
            // 保存数据到数仓
            case MyDataConstant.TASK_TYPE_SAVE_DATA -> new SaveDataToWarehouse();
            // 从数仓查询数据
            case MyDataConstant.TASK_TYPE_QUERY_DATA -> new QueryDataFromWarehouse();
            // 从数仓清空指定数据集合
            case MyDataConstant.TASK_TYPE_REMOVE_DATA -> new RemoveData();
            // 发送邮件
            case MyDataConstant.TASK_TYPE_SEND_EMAIL -> new SendEmail();
            // JSON值存入变量
            case MyDataConstant.TASK_TYPE_JSON_TO_VAR -> new ParseJsonToVar();
            // 设置变量
            case MyDataConstant.TASK_TYPE_SET_PIPELINE_VAR -> new SetPipelineVar();
            // JS脚本
            case MyDataConstant.TASK_TYPE_SCRIPT_JS -> new JsScript();
            // 其他不支持
            default -> throw new IllegalArgumentException("不支持的任务类型: " + task.getTaskType());
        };
    }

    /**
     * 执行指定的流水线任务
     *
     * @param taskId         流水线任务id
     * @param jobContextData 上下文数据
     */
    public final void execute(Long historyId, Long taskId, Long taskLogId, Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = taskService.getById(taskId);
        AssertUtil.notNull(pipelineTask);
        this.pipelineTask = pipelineTask;

        // 流水线id
        Long pipelineId = pipelineTask.getPipelineId();

        // 任务开始
        Date taskStartTime = new Date();
        PipelineLog pipelineLog = pipelineLogService.getById(taskLogId);
        // pipelineLog.setId(taskLogId);
        // pipelineLog.setPipelineId(pipelineId);
        // pipelineLog.setHistoryId(historyId);
        // pipelineLog.setTaskType(pipelineTask.getTaskType());
        // pipelineLog.setTaskName(pipelineTask.getTaskName());
        pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_READY);
        pipelineLog.setStartTime(taskStartTime);

        // 执行次数
        int executionCount = ObjectUtil.defaultIfNull(pipelineLog.getExecutionCount(), 0);
        executionCount++;
        pipelineLog.setExecutionCount(executionCount);

        this.pipelineLog = pipelineLog;

        try {
            // 任务禁用状态
            if (ObjectUtil.equals(pipelineTask.getStatus(), SysConstant.STATUS_DISABLED)) {
                // 禁用的任务 状态为跳过
                pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SKIP);
                info("该任务已禁用，不执行。");
                return;
            }
            
            info("========== 任务开始执行，第{}次 ==========", executionCount);

            // 更新任务日志的执行状态
            pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_RUNNING);
            pipelineLogService.updateById(pipelineLog);

            // 执行任务
            doExecute(jobContextData);

            // 执行成功
            pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_SUCCESS);
        } catch (StopPipelineException e) {
            // 停止流水线
            pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_STOPPED);
            // 抛出异常，结束流水线和后续任务
            throw e;
        } catch (Exception e) {
            // 异常，执行失败
            pipelineLog.setExecutionStatus(MyDataConstant.PIPELINE_HISTORY_STATUS_FAILED);
            // 记录异常
            error(e.getMessage());
            log.error(e.getMessage(), e);

            // 判断preCondition，默认成功才继续
            Integer preCondition = ObjectUtil.defaultIfNull(pipelineTask.getPreCondition(), MyDataConstant.PIPELINE_TASK_PRE_CONDITION_SUCCESS);
            // 若为 总是继续，则不抛出异常，继续下个task
            if (MyDataConstant.PIPELINE_TASK_PRE_CONDITION_ALWAYS == preCondition) {
                info("因任务设置为\"失败继续执行\"，流水线继续执行...");
                log.info("任务设置为 失败继续执行...");
                return;
            }

            // 抛出异常，结束流水线和后续任务
            throw e;
        } finally {
            // 任务执行结束
            // 更新任务日志的结束时间
            Date taskEndTime = new Date();
            pipelineLog.setEndTime(taskEndTime);

            // 计算任务执行的耗时
            pipelineLog.setExecutionTime(DateUtil.between(taskStartTime, taskEndTime, DateUnit.SECOND));

            try {
                // 更新任务日志
                pipelineLogService.updateById(pipelineLog);
            } catch (Exception e) {
                // 更新任务日志失败，则停止任务
                pipelineLogService.failLog(pipelineLog.getId());
                log.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * 外部调用执行的入口
     *
     * @param jobContextData 上下文数据
     */
    // public final void execute(Map<String, Object> jobContextData) {
    //     log("[{}] 开始执行", pipelineTask.getTaskName());
    //     if (MapUtil.isNotEmpty(jobContextData)) {
    //         // StringBuffer logInfo = new StringBuffer();
    //         // jobContextData.forEach((k, v) -> {
    //         //     logInfo.append(StringUtil.format("\t{} = {}\n", k, v));
    //         // });
    //         // log("输入参数：\n" + logInfo.toString());
    //     }
    //
    //     try {
    //         doExecute(jobContextData);
    //         log("[{}] 执行完成。", pipelineTask.getTaskName());
    //     } catch (StopPipelineException e) {
    //         log("[{}] 执行停止。", pipelineTask.getTaskName());
    //         throw e;
    //     } catch (Exception e) {
    //         error("[{}] 执行失败：{}", pipelineTask.getTaskName(), e.getMessage());
    //         throw e;
    //     }
    // }

    /**
     * 执行任务的抽象方法，子类实现具体逻辑
     */
    public abstract void doExecute(Map<String, Object> jobContextData);

    /**
     * 获取当前任务 操作数据的数据仓库名称
     *
     * @return 数据仓库名称
     */
    protected String getWarehouseName() {
        Project project = MyDataCache.getProject(pipelineTask.getProjectId());
        return MyDataUtil.getBizDbCode(pipelineTask.getTenantId(), project.getProjectCode());
    }

    /**
     * 获取任务配置中的字段映射
     *
     * @return 数据字段与接口字段的映射
     */
    protected Map<String, String> getFieldMapping() {
        if (pipelineTask == null) {
            return MapUtil.empty();
        }
        if (MapUtil.isEmpty(pipelineTask.getTaskConfig())) {
            return MapUtil.empty();
        }
        return (Map<String, String>) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_FIELD_MAPPING);
    }

    /**
     * 上下文的数据字段列表
     * 支持存储多个数据标准的字段
     */
    Map<Long, List<DataField>> dataFieldMap = MapUtil.newHashMap();

    /**
     * 获取标准数据的字段列表
     *
     * @param dataId 标准数据id
     * @return 字段列表
     */
    protected List<DataField> getDataFields(Long dataId) {
        if (dataFieldMap.containsKey(dataId)) {
            return dataFieldMap.get(dataId);
        }

        List<DataField> dataFields = dataFieldService.listByData(dataId);
        if (CollectionUtil.isEmpty(dataFields)) {
            // error("保存业务数据失败：标准数据没有字段");
            throw new RuntimeException("保存业务数据失败：标准数据没有字段");
        }
        dataFieldMap.put(dataId, dataFields);

        return dataFields;
    }

    /**
     * 获取任务配置
     *
     * @return 任务配置
     */
    protected Map<String, Object> getTaskConfig() {
        if (pipelineTask == null) {
            return MapUtil.empty();
        }
        return pipelineTask.getTaskConfig();
    }

    /**
     * 获取任务配置中的 输入 变量名配置
     *
     * @return 输入变量名配置
     */
    protected Map<String, String> getInputMap() {
        Map<String, Object> taskConfig = getTaskConfig();
        return (Map<String, String>) taskConfig.get(MyDataConstant.TASK_CONFIG_KEY_INPUT);
    }

    /**
     * 获取任务配置中的 输出 变量名配置
     *
     * @return 输出变量名配置
     */
    protected Map<String, String> getOutputMap() {
        if (pipelineTask == null) {
            return MapUtil.empty();
        }
        if (MapUtil.isEmpty(pipelineTask.getTaskConfig())) {
            return MapUtil.empty();
        }

        return (Map<String, String>) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_OUTPUT);
    }

    /**
     * 设置流水线上下文的json
     *
     * @param jobContextData 流水线上下文数据
     * @param pipelineJsons  流水线json
     */
    protected void setPipelineJson(Map<String, Object> jobContextData, List<PipelineJson> pipelineJsons) {
        Map<String, String> output = getOutputMap();
        String pipelineJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_PIPELINE_JSON);
        jobContextData.put(pipelineJsonKey, pipelineJsons);
    }

    /**
     * 从流水线上下文获取json
     *
     * @param jobContextData 流水线上下文数据
     * @return 流水线json
     */
    protected List<PipelineJson> getPipelineJson(Map<String, Object> jobContextData) {
        String pipelineJsonKey = getInputMap().get(MyDataConstant.JOB_DATA_KEY_PIPELINE_JSON);
        if (StringUtil.isEmpty(pipelineJsonKey)) {
//            error("JSON变量名为空，结束执行。");
            fail("输入参数无效：JSON变量名为空，结束执行。");
        }
        return (List<PipelineJson>) jobContextData.get(pipelineJsonKey);
    }

    /**
     * 记录任务正常日志
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    public void info(String message, Object... params) {
        if (pipelineLog != null) {
            if (ArrayUtil.isNotEmpty(params)) {
                for (int i = 0; i < params.length; i++) {
                    // 临时减少参数长度，减少日志内容
                    params[i] = StringUtil.sub(StringUtil.toStringOrEmpty(params[i]), 0, 10000);
                }
            }
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.nowInMillis() + "] [INFO] " + StringUtil.format(message, params));
        }
        log.info(message, params);
    }

    /**
     * 只记录任务异常日志，不抛异常
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    public void error(String message, Object... params) {
        if (pipelineLog != null) {
            if (ArrayUtil.isNotEmpty(params)) {
                for (int i = 0; i < params.length; i++) {
                    params[i] = StringUtil.sub(StringUtil.toStringOrEmpty(params[i]), 0, 10000);
                }
            }
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.nowInMillis() + "] [ERROR] " + StringUtil.format(message, params));
        }
        log.error(message, params);
    }

    /**
     * 任务失败，直接抛出异常<br/>
     * 后续在{@link TaskExecutor#execute(Long, Long, Long, Map)}中捕获异常，再统一记录日志
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    public void fail(String message, Object... params) throws RuntimeException {
        throw new RuntimeException(StringUtil.format(message, params));
    }
}
