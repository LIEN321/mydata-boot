package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.warehouse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.List;
import java.util.Map;

/**
 * Json转为业务数据
 *
 * @author LIEN
 * @since 2024/11/29
 */
public class ParseJsonToData extends TaskExecutor {
    public ParseJsonToData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 输入配置
        Map<String, String> inputMap = getInputMap();
        if (MapUtil.isEmpty(inputMap)) {
            error("未配置输入参数，结束执行。");
            throw new IllegalArgumentException("未配置输入参数，结束执行。");
        }

        // 获取待解析json的key
        String inputDataKey = inputMap.get(MyDataConstant.TASK_DATA_KEY_DATA_JSON);
        if (StringUtil.isEmpty(inputDataKey)) {
            error("JSON变量名为空，结束执行。");
            throw new IllegalArgumentException("JSON变量名为空，结束执行。");
        }

        // 字段映射
        Map<String, String> fieldMapping = getFieldMapping();
        if (CollectionUtil.isEmpty(fieldMapping)) {
            error("字段映射为空，结束执行。");
            throw new IllegalArgumentException("字段映射为空，结束执行。");
        }

        // 从上下文获取json
        List<String> dataJsonList = (List<String>) jobContextData.get(inputDataKey);
        if (CollectionUtil.isEmpty(dataJsonList)) {
            log("没有JSON待转换，结束执行。");
            return;
        }

        // 输出配置
        Map<String, String> outputMap = getOutputMap();
        String bizDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("输出设置中的业务数据变量名为空，结束执行。");
            throw new IllegalArgumentException("输出设置中的业务数据变量名为空，结束执行。");
        }

        // 业务数据集合
        List<Map<String, Object>> bizDataList = CollUtil.newArrayList();

        for (String dataJsonString : dataJsonList) {
            log("开始转换JSON：{}", dataJsonString);
            // 业务数据的json对象
            JSON dataJson = JsonUtil.parse(dataJsonString);

            // 使用数组模式 兼容单个对象和数组模式
            JSONArray jsonArray;
            if (dataJson instanceof JSONArray) {
                jsonArray = (JSONArray) dataJson;
            } else {
                jsonArray = new JSONArray();
                jsonArray.add(dataJson);
            }

            // 根据映射 解析出json中的数据 并存入数据
            jsonArray.forEach(obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                Map<String, Object> produceData = MapUtil.newHashMap();
                fieldMapping.forEach((dataFieldCode, apiFieldCode) -> {
                    // 若字段映射中 未设置api参数名，则跳过处理；
                    if (StrUtil.isEmpty(apiFieldCode)) {
                        return;
                    }

                    // 获取业务数据值
                    Object value;
                    // TODO /field 根目录格式
//                if (StringUtil.startWith(apiFieldCode, MyDataConstant.FIELD_MAPPING_ROOT)) {
//                    value = baseJson.getByPath(apiFieldCode.substring(MyDataConstant.FIELD_MAPPING_ROOT.length()));
//                } else {
                    value = jsonObject.getByPath(apiFieldCode);
//                }
                    // TODO 未获取到值，再解析属性表达式 从任务变量尝试获取数据
//                    if (value == null && JobVarService.isFieldExp(apiCode)) {
//                        value = JobVarService.parseDataFieldVar(apiCode, taskJob.getTaskVar(), taskJob.getFieldTypeMapping());
//                    }
                    // 若接口数据中 没有执行的字段名，则跳过处理
                    if (value == null) {
                        return;
                    }

//                    String targetType = fieldTypeMapping.get(standardCode);
//                    try {
//                        produceData.put(standardCode, MdUtil.convertDataType(value, targetType));
//                    } catch (Exception e) {
//                        taskJob.appendLog("转换业务数据出错，数据：{}，字段 {} 转为目标类型 {} 时出错：{}", obj, standardCode, targetType, e.getMessage());
//                    }
                    produceData.put(dataFieldCode, value);
                });

                // TODO 补充默认字段值
//                if (CollUtil.isNotEmpty(fieldDefaultValues)) {
//                    fieldDefaultValues.forEach((fieldCode, fieldDefaultValue) -> {
//                        if (produceData.containsKey(fieldCode)) {
//                            return;
//                        }
//
//                        String targetType = fieldTypeMapping.get(fieldCode);
//                        produceData.put(fieldCode, MdUtil.convertDataType(fieldDefaultValue, targetType));
//                    });
//                }
                bizDataList.add(produceData);
            });
        }

        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();
        // 获取标准数据信息
        Data data = MyDataCache.getData(pipelineTask.getDataId());

        // 数据存入任务上下文数据中
        jobContextData.put(MyDataConstant.JOB_DATA_KEY_BIZ_DATA, bizDataList);
        log("共获得数据 {} 条，内容为：{}", bizDataList.size(), bizDataList);

        jobContextData.put(MyDataConstant.JOB_DATA_KEY_DATA_ID, pipelineTask.getDataId());
        jobContextData.put(MyDataConstant.JOB_DATA_KEY_DATA_CODE, data.getDataCode());
    }
}
