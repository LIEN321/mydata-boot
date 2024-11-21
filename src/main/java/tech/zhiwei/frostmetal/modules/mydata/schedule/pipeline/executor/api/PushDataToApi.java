package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;

import java.util.Map;

/**
 * 向API发送数据
 *
 * @author LIEN
 * @since 2024/11/22
 */
public class PushDataToApi extends TaskExecutor {
    public PushDataToApi(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public Map<String, Object> execute() {
        return null;
    }
}
