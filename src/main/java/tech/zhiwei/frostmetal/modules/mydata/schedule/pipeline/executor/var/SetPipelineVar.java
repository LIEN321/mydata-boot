package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var;

import cn.hutool.extra.expression.ExpressionUtil;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineContext;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobVarService;
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
    // public SetPipelineVar(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(PipelineContext pipelineContext) {
        // 变量配置
        List<Map<String, String>> varMappings = (List<Map<String, String>>) getTaskConfig().get(MyDataConstant.JOB_DATA_VAR_MAPPING);
        if (CollectionUtil.isEmpty(varMappings)) {
//            error("变量配置为空，结束执行。");
            throw new IllegalArgumentException("变量配置为空，结束执行。");
        }

        varMappings.forEach(varMapping -> {
            String varCode = varMapping.get("varCode");
            String varType = StringUtil.emptyIfNull(varMapping.get("varType"));
            if (StringUtil.isNotEmpty(varCode)) {
                // 替换${var.property}表达式的值
                Object varValue = JobVarService.processDataFieldVar(varMapping.get("varValue"), pipelineContext.getMap());
                // 支持运算表达式
                try {
                    varValue = ExpressionUtil.eval(varValue.toString(), pipelineContext.getMap());
                } catch (Exception e) {
                    // 忽略表达式解析异常，普通字符串、日期等内容不计算
                }
                // 转为指定类型
                varValue = MyDataUtil.convertDataType(varValue, varType);
                pipelineContext.put(varCode, varValue);
                info("设置变量 {} = {}", varCode, varValue);
            }
        });
    }
}
