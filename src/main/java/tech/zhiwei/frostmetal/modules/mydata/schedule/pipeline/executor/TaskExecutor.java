package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.GetJsonFromWebhook;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.PushDataToApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.ParseJsonToData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse.SaveDataToWarehouse;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Map;

/**
 * 流水线任务执行器
 *
 * @author LIEN
 * @since 2024/11/21
 */
public abstract class TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    private PipelineTask pipelineTask;

    public TaskExecutor(PipelineTask pipelineTask) {
        this.pipelineTask = pipelineTask;
    }

    public PipelineTask getPipelineTask() {
        return pipelineTask;
    }

    public static TaskExecutor getExecutor(PipelineTask pipelineTask) {
        switch (pipelineTask.getTaskType()) {
            // 从API获取JSON
            case MyDataConstant.TASK_TYPE_API_GET_JSON -> {
                return new GetJsonFromApi(pipelineTask);
            }
            // 向API发送数据
            case MyDataConstant.TASK_TYPE_API_SEND_DATA -> {
                return new PushDataToApi(pipelineTask);
            }
            case MyDataConstant.TASK_TYPE_WEBHOOK_GET_JSON -> {
                return new GetJsonFromWebhook(pipelineTask);
            }
            case MyDataConstant.TASK_TYPE_API_GET_VAR -> {
                log.info("TASK_TYPE_API_GET_VAR");
            }

            // JSON转数据
            case MyDataConstant.TASK_TYPE_JSON_TO_DATA -> {
                return new ParseJsonToData(pipelineTask);
            }
            case MyDataConstant.TASK_TYPE_FILTER_DATA -> {
                log.info("TASK_TYPE_FILTER_DATA");
            }
            case MyDataConstant.TASK_TYPE_OPERATE_DATA -> {
                log.info("TASK_TYPE_OPERATE_DATA");
            }
            case MyDataConstant.TASK_TYPE_WRITE_EXCEL -> {
                log.info("TASK_TYPE_WRITE_EXCEL");
            }

            // 保存数据到数仓
            case MyDataConstant.TASK_TYPE_SAVE_DATA -> {
                return new SaveDataToWarehouse(pipelineTask);
            }
            case MyDataConstant.TASK_TYPE_QUERY_DATA -> {
                log.info("TASK_TYPE_QUERY_DATA");
            }

            case MyDataConstant.TASK_TYPE_SEND_EMAIL -> {
                log.info("TASK_TYPE_SEND_EMAIL");
            }
            default -> {
                log.error("不支持的任务类型：" + pipelineTask.getTaskType());
            }
        }

        throw new RuntimeException("无法获取任务执行器！");
    }

    /**
     * 执行流水线任务
     */
    public abstract void execute(Map<String, Object> jobContextData);

    /**
     * 获取任务配置中的字段映射
     *
     * @return 数据字段与接口字段的映射
     */
    public Map<String, String> getFieldMapping() {
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
    public Map<String, String> getInputMap() {
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
    public Map<String, String> getOutputMap() {
        if (pipelineTask == null) {
            return MapUtil.empty();
        }
        if (MapUtil.isEmpty(pipelineTask.getTaskConfig())) {
            return MapUtil.empty();
        }

        return (Map<String, String>) pipelineTask.getTaskConfig().get(MyDataConstant.TASK_CONFIG_KEY_OUTPUT);
    }
}
