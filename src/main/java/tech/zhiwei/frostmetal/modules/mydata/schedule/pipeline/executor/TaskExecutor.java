package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Project;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IDataFieldService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.SendDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.email.SendEmail;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline.TriggerPipeline;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.FilterData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseDataToJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseJsonToData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ProcessData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.WriteDataToExcel;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var.ParseJsonToVar;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.QueryDataFromWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.SaveDataToWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.webhook.GetJsonFromWebhook;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.util.ArrayUtil;

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
public abstract class TaskExecutor {
    private final PipelineTask pipelineTask;
    private final PipelineLog pipelineLog;
    private final IDataFieldService dataFieldService = SpringUtil.getBean(IDataFieldService.class);

    public TaskExecutor(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        this.pipelineTask = pipelineTask;
        this.pipelineLog = pipelineLog;
    }

    // 工厂方法
    public static TaskExecutor create(PipelineTask task, PipelineLog log) {
        return switch (task.getTaskType()) {
            // 从API获取JSON
            case MyDataConstant.TASK_TYPE_API_GET_JSON -> new GetJsonFromApi(task, log);
            // 向API发送数据
            case MyDataConstant.TASK_TYPE_API_SEND_DATA -> new SendDataToApi(task, log);
            // 从Webhook接收JSON
            case MyDataConstant.TASK_TYPE_WEBHOOK_GET_JSON -> new GetJsonFromWebhook(task, log);
            // 用Webhook触发流水线
            case MyDataConstant.TASK_TYPE_TRIGGER_PIPELINE -> new TriggerPipeline(task, log);
            // JSON转数据
            case MyDataConstant.TASK_TYPE_JSON_TO_DATA -> new ParseJsonToData(task, log);
            // 数据转JSON
            case MyDataConstant.TASK_TYPE_DATA_TO_JSON -> new ParseDataToJson(task, log);
            // 过滤数据
            case MyDataConstant.TASK_TYPE_FILTER_DATA -> new FilterData(task, log);
            // 处理数据
            case MyDataConstant.TASK_TYPE_PROCESS_DATA -> new ProcessData(task, log);
            // 数据写入Excel
            case MyDataConstant.TASK_TYPE_WRITE_EXCEL -> new WriteDataToExcel(task, log);
            // 保存数据到数仓
            case MyDataConstant.TASK_TYPE_SAVE_DATA -> new SaveDataToWarehouse(task, log);
            // 从数仓查询数据
            case MyDataConstant.TASK_TYPE_QUERY_DATA -> new QueryDataFromWarehouse(task, log);
            // 发送邮件
            case MyDataConstant.TASK_TYPE_SEND_EMAIL -> new SendEmail(task, log);
            // JSON值存入变量
            case MyDataConstant.TASK_TYPE_JSON_TO_VAR -> new ParseJsonToVar(task, log);
            default -> throw new IllegalArgumentException("不支持的任务类型: " + task.getTaskType());
        };
    }

    /**
     * 外部调用执行的入口
     *
     * @param jobContextData 上下文数据
     */
    public final void execute(Map<String, Object> jobContextData) {
        log("[{}] 开始执行", pipelineTask.getTaskName());
        if (MapUtil.isNotEmpty(jobContextData)) {
            log("输入参数：");
            jobContextData.forEach((k, v) -> {
                log("{} = {}", k, v);
            });
        }

        try {
            doExecute(jobContextData);
            log("[{}] 执行完成。", pipelineTask.getTaskName());
        } catch (Exception e) {
            error("[{}] 执行失败：{}", pipelineTask.getTaskName(), e.getMessage());
            throw e;
        }
    }

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
            error("保存业务数据失败：标准数据没有字段");
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
            throw new IllegalArgumentException("JSON变量名为空，结束执行。");
        }
        return (List<PipelineJson>) jobContextData.get(pipelineJsonKey);
    }

    /**
     * 记录任务正常日志
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    public void log(String message, Object... params) {
        if (pipelineLog != null) {
            if (ArrayUtil.isNotEmpty(params)) {
                for (int i = 0; i < params.length; i++) {
                    params[i] = StringUtil.sub(StringUtil.toStringOrEmpty(params[i]), 0, 10000);
                }
            }
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.nowInMillis() + "] [INFO] " + StringUtil.format(message, params));
        }
        log.info(message, params);
    }

    /**
     * 记录任务异常日志
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
}
