package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process;

import cn.hutool.json.JSON;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * 业务数据转为JSON
 *
 * @author LIEN
 * @since 2024/11/29
 */
@Slf4j
public class ParseDataToJson extends TaskExecutor {

    public ParseDataToJson(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        // 输入配置
        Map<String, String> inputMap = getInputMap();

        // 获取转换数据的key
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("执行失败：未配置待转换的业务数据变量，无法获取业务数据");
            throw new IllegalArgumentException("执行失败：未配置待转换的业务数据变量，无法获取业务数据");
        }

        // 上下文业务数据
        PipelineBizData pipelineBizData = (PipelineBizData) jobContextData.get(bizDataKey);
        if (pipelineBizData == null) {
            error("执行失败：前置任务没有输出有效的业务数据");
            throw new IllegalArgumentException("执行失败：前置任务没有输出有效的业务数据");
        }

        // 字段映射
        Map<String, String> fieldMapping = getFieldMapping();

        // 输出配置
        Map<String, String> outputMap = getOutputMap();
        String dataJsonKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_DATA_JSON);
        if (StringUtil.isEmpty(dataJsonKey)) {
            error("执行失败：无效的输出设置，未配置JSON的变量名");
            throw new RuntimeException("执行失败：无效的输出设置，未配置JSON的变量名");
        }

        // 复制上下文的业务数据
        List<Map<String, Object>> bizDataList = ObjectUtil.cloneByStream(pipelineBizData.getBizData());
        // 如果字段映射有效，则根据字段映射 修改业务数据的key
        if (CollectionUtil.isNotEmpty(fieldMapping)) {
            bizDataList.forEach(bizData -> {
                fieldMapping.forEach((dataFiledCode, jsonFieldCode) -> {
                    // 若数据字段没有配置映射的json属性，则不作为结果返回
                    // 先取出数据
                    Object value = bizData.remove(dataFiledCode);
                    // 若配置json属性，则写入
                    if (StringUtil.isNotEmpty(jsonFieldCode)) {
                        // 业务数据字段值 转移到 json属性名下
                        bizData.put(jsonFieldCode, value);
                    }
                });
            });
        }

        log("业务数据：{}", bizDataList);
        JSON dataJson = JsonUtil.parse(bizDataList);

        // JSON模板
        String jsonTemplate = StringUtil.nullToEmpty((String) pipelineTask.getTaskConfig().get("JSON_TEMPLATE"));
        log("JSON模板：{}", jsonTemplate);

        // 将json字符串 替换${DATA_JSON}占位符
        String json = StringUtil.substitute(jsonTemplate, MyDataConstant.JOB_DATA_KEY_DATA_JSON, dataJson.toString());

        // 数据存入任务上下文数据中
        jobContextData.put(dataJsonKey, json);
        log("转换后的JSON：{}", json);
    }
}
