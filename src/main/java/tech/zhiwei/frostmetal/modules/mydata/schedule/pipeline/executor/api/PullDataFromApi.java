package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.zhiwei.frostmetal.modules.mydata.cache.MdCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从API读取数据
 *
 * @author LIEN
 * @since 2024/11/21
 */
public class PullDataFromApi extends TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(PullDataFromApi.class);

    public PullDataFromApi(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public Map<String, Object> execute() {
        log.info("从API获取数据 开始");

        PipelineTask pipelineTask = getPipelineTask();
        // 获取应用信息
        App app = MdCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        // 获取接口信息
        AppApi api = MdCache.getApi(pipelineTask.getApiId());
        String apiUrl = api.getApiUri();
        if (StringUtil.isNotEmpty(apiPrefix)) {
            apiUrl = apiPrefix + apiUrl;
        }

        // TODO 分批模式

        // 调用api
        String responseJson = HttpUtil.send(api.getApiMethod(), apiUrl, null, null, null, null);
        log.info("API获取结果：{}", responseJson);

        List<Map<String, Object>> fieldMappings = (List<Map<String, Object>>) pipelineTask.getTaskConfig().get("fieldMappings");
        // 字段映射
        Map<String, String> fieldMapping = MapUtil.newHashMap();
        fieldMappings.forEach(map -> {
            fieldMapping.put(map.get("fieldCode").toString(), map.get("apiField").toString());
        });

        if (CollectionUtil.isEmpty(fieldMapping)) {
            throw new IllegalArgumentException("字段映射为空！");
        }

        // 字段层级前缀
        String apiFieldPrefix = api.getFieldPrefix();

        // 最初的json对象
        JSON originJson = JsonUtil.parse(responseJson);

        // 使用数组模式 兼容单个对象和数组模式
        JSONArray baseArray;
        if (originJson instanceof JSONArray) {
            baseArray = (JSONArray) originJson;
        } else {
            baseArray = new JSONArray();
            baseArray.add(originJson);
        }

        // 业务数据集合
        List<Map<String, Object>> produceDataList = CollUtil.newArrayList();

        baseArray.forEach(json -> {
            // 保留根目录json，用于 /field 格式提取数据
            JSON baseJson = (JSONObject) json;
            // 根据配置的prefix 定位到数据层级
            JSON dataJson = baseJson;
            if (StrUtil.isNotEmpty(apiFieldPrefix)) {
                Object prefixJson = baseJson.getByPath(apiFieldPrefix);
                if (!(prefixJson instanceof JSON)) {
                    throw new RuntimeException("接口前缀 无法解析为JSON");
                }
                dataJson = (JSON) prefixJson;
            }
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
                fieldMapping.forEach((standardCode, apiCode) -> {
                    // 若字段映射中 未设置api参数名，则跳过处理；
                    if (StrUtil.isEmpty(apiCode)) {
                        return;
                    }

                    // 获取业务数据值
                    Object value;
                    // /field 根目录格式
                    if (StringUtil.startWith(apiCode, MdConstant.FIELD_MAPPING_ROOT)) {
                        value = baseJson.getByPath(apiCode.substring(MdConstant.FIELD_MAPPING_ROOT.length()));
                    } else {
                        value = jsonObject.getByPath(apiCode);
                    }
                    // 未获取到值，再解析属性表达式 从任务变量尝试获取数据
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
                    produceData.put(standardCode, value);
                });

                // 补充默认字段值
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
                produceDataList.add(produceData);
            });
        });

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("BIZ_DATA", produceDataList);
        return resultMap;
    }
}