package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Data;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON转为业务数据
 *
 * @author LIEN
 * @since 2024/11/29
 */
@Slf4j
public class ParseJsonToData extends TaskExecutor {

    // public ParseJsonToData(PipelineTask pipelineTask, PipelineLog pipelineLog) {
    //     super(pipelineTask, pipelineLog);
    // }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        // 当前流水线任务
        PipelineTask pipelineTask = getPipelineTask();

        // 输入配置
        Map<String, String> inputMap = getInputMap();

        // 从上下文获取json
        // 业务数据json
        List<PipelineJson> pipelineJsons = getPipelineJson(jobContextData);
        if (ObjectUtil.isEmpty(pipelineJsons)) {
            info("没有JSON待转换，结束执行。");
            return;
        }

        // 字段映射
        Map<String, String> fieldMapping = getFieldMapping();
        if (CollectionUtil.isEmpty(fieldMapping)) {
//            error("字段映射为空，结束执行。");
            throw new IllegalArgumentException("字段映射为空，结束执行。");
        }
        info("字段映射：{}", fieldMapping);

        // 输出配置
        Map<String, String> outputMap = getOutputMap();
        String bizDataKey = outputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
//            error("输出设置中的业务数据变量名为空，结束执行。");
            throw new IllegalArgumentException("输出设置中的业务数据变量名为空，结束执行。");
        }

        // 获取标准数据信息
        Long dataId = pipelineTask.getDataId();
        Data data = MyDataCache.getData(dataId);
        // 标准数据字段列表
        List<DataField> dataFields = getDataFields(dataId);
        // 字段编号-字段类型
        Map<String, String> fieldTypeMapping = dataFields.stream().collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));

        // 业务数据集合
        List<Map<String, Object>> bizDataList = CollUtil.newArrayList();
        // 字段映射中用到的字段列表
        List<DataField> usedDataFields = dataFields.stream()
                .filter(field -> fieldMapping.containsKey(field.getFieldCode()))
                .toList();

        // 用户新配置的 标识字段
        List<String> newIdFields = (List<String>) getTaskConfig().get("ID_FIELD");
        if (CollectionUtil.isNotEmpty(newIdFields)) {
            usedDataFields.forEach(dataField -> {
                dataField.setIsId(newIdFields.contains(dataField.getFieldCode()));
            });
        }

        pipelineJsons.forEach(pipelineJson -> {
            // 获取原始json
            JSON originJson = pipelineJson.getOriginJson();
            // log("接收的原始json：{}", originJson);

            // 获取业务数据json
            List<JSON> dataJsonList = pipelineJson.getDataJsonList();
            // log("接收的数据json：{}", dataJsonList);

            for (JSON dataJson : dataJsonList) {
                JSONArray dataJsons = new JSONArray();
                if (dataJson instanceof JSONObject) {
                    dataJsons.add(dataJson);
                } else if (dataJson instanceof JSONArray) {
                    dataJsons = (JSONArray) dataJson;
                }

                // 根据映射 解析出json中的数据 并存入数据
                dataJsons.forEach(o -> {
                    JSONObject jsonObject = (JSONObject) o;
                    Map<String, Object> produceData = MapUtil.newHashMap();
                    fieldMapping.forEach((dataFieldCode, apiFieldCode) -> {
                        // 若字段映射中 未设置api参数名，则跳过处理；
                        if (StrUtil.isEmpty(apiFieldCode)) {
                            return;
                        }

                        // 获取业务数据值
                        Object value;
                        // /field 根目录格式
                        if (StringUtil.startWith(apiFieldCode, MyDataConstant.FIELD_MAPPING_ROOT)) {
                            value = originJson.getByPath(apiFieldCode.substring(MyDataConstant.FIELD_MAPPING_ROOT.length()));
                        } else {
                            value = jsonObject.getByPath(apiFieldCode);
                        }
                        // TODO 未获取到值，再解析属性表达式 从任务变量尝试获取数据
//                    if (value == null && JobVarService.isFieldExp(apiCode)) {
//                        value = JobVarService.parseDataFieldVar(apiCode, taskJob.getTaskVar(), taskJob.getFieldTypeMapping());
//                    }
                        // 若接口数据中 没有执行的字段名，则跳过处理
                        if (value == null) {
                            return;
                        }

                        String targetType = fieldTypeMapping.get(dataFieldCode);
                        try {
                            produceData.put(dataFieldCode, MyDataUtil.convertDataType(value, targetType));
                        } catch (Exception e) {
//                        error("转换业务数据出错，数据：{}，字段 {} 转为目标类型 {} 时出错：{}", jsonObject, dataFieldCode, targetType, e.getMessage());
                            throw new RuntimeException(StringUtil.format("转换业务数据出错，数据：{}，字段 {} 转为目标类型 {} 时出错：{}", jsonObject, dataFieldCode, targetType, e.getMessage()));
                        }
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
                    if (!produceData.isEmpty()) {
                        bizDataList.add(produceData);
                    }
                });
            }
        });

        // 数据存入任务上下文数据中
        PipelineBizData pipelineBizData = new PipelineBizData(dataId, data.getDataCode(), usedDataFields, bizDataList);
        jobContextData.put(bizDataKey, pipelineBizData);

        // log("共获得数据 {} 条，内容为：{}", bizDataList.size(), bizDataList);
        info("共获得数据 {} 条", bizDataList.size());
    }
}
