package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api.LoadDataFromApi;

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
            case MdConstant.TASK_TYPE_API_GET_DATA -> {
                log.info("TASK_TYPE_API_GET_DATA");
                return new LoadDataFromApi(pipelineTask);
            }
            case MdConstant.TASK_TYPE_API_SEND_DATA -> {
                log.info("TASK_TYPE_API_SEND_DATA");
            }
            case MdConstant.TASK_TYPE_WEBHOOK_GET_DATA -> {
                log.info("TASK_TYPE_WEBHOOK_GET_DATA");
            }
            case MdConstant.TASK_TYPE_API_GET_VAR -> {
                log.info("TASK_TYPE_API_GET_VAR");
            }
            case MdConstant.TASK_TYPE_SAVE_DATA -> {
                log.info("TASK_TYPE_SAVE_DATA");
            }
            case MdConstant.TASK_TYPE_QUERY_DATA -> {
                log.info("TASK_TYPE_QUERY_DATA");
            }
            case MdConstant.TASK_TYPE_FILTER_DATA -> {
                log.info("TASK_TYPE_FILTER_DATA");
            }
            case MdConstant.TASK_TYPE_OPERATE_DATA -> {
                log.info("TASK_TYPE_OPERATE_DATA");
            }
            case MdConstant.TASK_TYPE_WRITE_EXCEL -> {
                log.info("TASK_TYPE_WRITE_EXCEL");
            }
            case MdConstant.TASK_TYPE_SEND_EMAIL -> {
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
    public abstract Object execute();
}
