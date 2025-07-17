package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.pipeline;

import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineCondition;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.StopPipelineException;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.frostmetal.modules.mydata.util.StringParser;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * 根据配置的条件 停止流水线
 *
 * @author LIEN
 * @since 2025/7/14
 */
@Slf4j
public class StopPipeline extends TaskExecutor {

    public StopPipeline(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        // 配置的条件
        List<Map<String, Object>> stopConditions = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("STOP_CONDITION");
        if (CollectionUtil.isEmpty(stopConditions)) {
            log("未配置结束条件，继续执行");
            return;
        }

        List<PipelineCondition> pipelineConditions = MyDataUtil.convertPipelineCondition(stopConditions);
        if (pipelineConditions == null) {
            return;
        }

        // 遍历条件，有一个成立 则结束流水线
        pipelineConditions.forEach(condition -> {
            String key = condition.getKey();
            Object param = jobContextData.get(key);
            String op = condition.getOp();
            Object value = StringParser.autoParse((String) condition.getValue());
            if (MyDataUtil.compare(param, op, value)) {
                throw new StopPipelineException(StringUtil.format("满足终止条件：{} {} {}，终止流水线", key, op, value));
            }
            log("不满足终止条件：{} {} {}，继续...", param, op, value);
        });
    }
}
