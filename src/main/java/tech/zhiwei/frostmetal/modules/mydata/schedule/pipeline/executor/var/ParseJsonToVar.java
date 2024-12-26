package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * JSON值存入变量
 *
 * @author LIEN
 * @since 2024/12/26
 */
@Slf4j
public class ParseJsonToVar extends TaskExecutor {

    public ParseJsonToVar(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 输入配置
        Map<String, String> inputMap = getInputMap();

        // 获取待解析json的key
        String pipelineJsonKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_PIPELINE_JSON);
        if (StringUtil.isEmpty(pipelineJsonKey)) {
            error("JSON变量名为空，结束执行。");
            throw new IllegalArgumentException("JSON变量名为空，结束执行。");
        }

        // 从上下文获取json
        // 业务数据json
        List<PipelineJson> pipelineJsons = (List<PipelineJson>) jobContextData.get(pipelineJsonKey);
        if (CollectionUtil.isEmpty(pipelineJsons)) {
            error("没有JSON待转换，结束执行。");
            throw new IllegalArgumentException("没有JSON待转换，结束执行。");
        }

        // 变量配置
        List<Map<String, String>> varMappings = (List<Map<String, String>>) getTaskConfig().get(MyDataConstant.JOB_DATA_VAR_MAPPING);
        if (CollectionUtil.isEmpty(varMappings)) {
            error("变量配置为空，结束执行。");
            throw new IllegalArgumentException("字段映射为空，结束执行。");
        }

        for (PipelineJson pipelineJson : pipelineJsons) {
            JSONObject originJson = pipelineJson.getOriginJson();

            varMappings.forEach(varMapping -> {
                String varCode = varMapping.get("varCode");
                String jsonField = varMapping.get("jsonField");
                Object varValue = originJson.getByPath(jsonField);
                jobContextData.put(varCode, varValue);
                log("设置变量 {} = {}", varCode, varValue);
            });
        }
    }
}
