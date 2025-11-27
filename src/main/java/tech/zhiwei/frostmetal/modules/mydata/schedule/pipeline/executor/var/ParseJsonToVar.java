package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.var;

import cn.hutool.json.JSON;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process.ProcessData;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.ObjectUtil;

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

    // public ParseJsonToVar(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 变量配置
        List<Map<String, Object>> varMappings = (List<Map<String, Object>>) getTaskConfig().get(MyDataConstant.JOB_DATA_VAR_MAPPING);
        if (CollectionUtil.isEmpty(varMappings)) {
//            error("变量配置为空，结束执行。");
            throw new IllegalArgumentException("字段映射为空，结束执行。");
        }

        // 从上下文获取json
        List<PipelineJson> pipelineJsons = getPipelineJson(jobContextData);
        if (ObjectUtil.isEmpty(pipelineJsons)) {
//            throw new IllegalArgumentException("没有JSON待转换，结束执行。");
            error("没有JSON待转换，结束执行。");
            return;
        }

        pipelineJsons.forEach(pipelineJson -> {
            // 获取原始json
            JSON originJson = pipelineJson.getOriginJson();
            if (JsonUtil.isEmpty(originJson)) {
                error("未获取有效json");
                return;
            }
            // log("从json提取值到变量，json = {}", originJson);

            // 遍历参数映射
            varMappings.forEach(varMapping -> {
                // 参数名
                String varCode = (String) varMapping.get("varCode");
                // json字段名
                String jsonField = (String) varMapping.get("jsonField");
//                Boolean switchEmpty = ObjectUtil.defaultIfNull((Boolean) varMapping.get("switchEmpty"), false);
                String op = (String) varMapping.get("op");

                Object varValue = originJson.getByPath(jsonField);
                varValue = ProcessData.processValue(varValue, op, null, null);

                if (ObjectUtil.isNotNull(varValue)) {
                    jobContextData.put(varCode, varValue);
                    log("设置变量成功：{} = {}", varCode, varValue);
                } else {
                    log("设置变量失败：{} = null", varCode);
                }
            });
        });
    }
}
