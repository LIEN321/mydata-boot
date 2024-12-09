package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromWebhook;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.SendDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.FilterData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ParseJsonToData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.QueryDataFromWarehouse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.SaveDataToWarehouse;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

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
            // JSON转数据
            case MyDataConstant.TASK_TYPE_JSON_TO_DATA -> new ParseJsonToData(task, log);
            // 过滤数据
            case MyDataConstant.TASK_TYPE_FILTER_DATA -> new FilterData(task, log);
            // 保存数据到数仓
            case MyDataConstant.TASK_TYPE_SAVE_DATA -> new SaveDataToWarehouse(task, log);
            // 从数仓查询数据
            case MyDataConstant.TASK_TYPE_QUERY_DATA -> new QueryDataFromWarehouse(task, log);
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
     * 获取任务配置中的 输入 变量名配置
     *
     * @return 输入变量名配置
     */
    protected Map<String, String> getInputMap() {
        if (pipelineTask == null) {
            return MapUtil.empty();
        }
        if (MapUtil.isEmpty(pipelineTask.getTaskConfig())) {
            return MapUtil.empty();
        }

        return (Map<String, String>) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_INPUT);
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
     * 记录任务正常日志
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    protected void log(String message, Object... params) {
        if (pipelineLog != null) {
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.now() + "] [INFO] " + StringUtil.format(message, params));
        }
        log.info(message, params);
    }

    /**
     * 记录任务异常日志
     *
     * @param message 日志内容
     * @param params  占位符参数值
     */
    protected void error(String message, Object... params) {
        if (pipelineLog != null) {
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.now() + "] [ERROR] " + StringUtil.format(message, params));
        }
        log.error(message, params);
    }
}
