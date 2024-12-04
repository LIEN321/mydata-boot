package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromWebhook;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.PushDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.ParseJsonToData;
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
public abstract class TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
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
            case MyDataConstant.TASK_TYPE_API_SEND_DATA -> new PushDataToApi(task, log);
            // 从Webhook接收JSON
            case MyDataConstant.TASK_TYPE_WEBHOOK_GET_JSON -> new GetJsonFromWebhook(task, log);
            // JSON转数据
            case MyDataConstant.TASK_TYPE_JSON_TO_DATA -> new ParseJsonToData(task, log);
            // 保存数据到数仓
            case MyDataConstant.TASK_TYPE_SAVE_DATA -> new SaveDataToWarehouse(task, log);
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
            log("[{}] 执行失败：{}", pipelineTask.getTaskName(), e.getMessage());
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
     * 记录任务日志
     *
     * @param message
     */
    protected void log(String message, Object... params) {
        if (pipelineLog != null) {
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.now() + "] [INFO] " + StringUtil.format(message, params));
        }
    }

    protected void error(String message, Object... params) {
        if (pipelineLog != null) {
            String existingLog = pipelineLog.getTaskLog();
            pipelineLog.setTaskLog((existingLog == null ? "" : existingLog + "\n") + "[" + DateUtil.now() + "] [ERROR] " + StringUtil.format(message, params));
        }
    }
}
