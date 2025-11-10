package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var;

import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * 设置流水线变量
 *
 * @author LIEN
 * @since 2025/9/19
 */
public class SetPipelineVar extends TaskExecutor {
    public SetPipelineVar(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 变量配置
        List<Map<String, String>> varMappings = (List<Map<String, String>>) getTaskConfig().get(MyDataConstant.JOB_DATA_VAR_MAPPING);
        if (CollectionUtil.isEmpty(varMappings)) {
//            error("变量配置为空，结束执行。");
            throw new IllegalArgumentException("变量配置为空，结束执行。");
        }

        varMappings.forEach(varMapping -> {
            String varCode = varMapping.get("varCode");
            String varType = StringUtil.nullToDefault(varMapping.get("varType"), MyDataConstant.DATA_TYPE_STRING);
            if (StringUtil.isNotEmpty(varCode)) {
                Object varValue = MyDataUtil.convertDataType(varMapping.get("varValue"), varType);
                jobContextData.put(varCode, varValue);
                log("设置变量 {} = {}", varCode, varValue);
            }
        });
    }
}
