package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.util.HashUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobBatchService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;
import tech.zhiwei.tool.thread.ThreadUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 从API获取json
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Slf4j
public class GetJsonFromApi extends TaskExecutor {

    private final JobBatchService jobBatchService = SpringUtil.getBean(JobBatchService.class);

    public GetJsonFromApi(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public void execute(Map<String, Object> jobContextData) {
        log.info("从API获取数据 开始");

        PipelineTask pipelineTask = getPipelineTask();
        // 获取应用信息
        App app = MyDataCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        // 获取接口信息
        AppApi api = MyDataCache.getApi(pipelineTask.getApiId());
        String apiUrl = api.getApiUri();
        if (StringUtil.isNotEmpty(apiPrefix)) {
            apiUrl = apiPrefix + apiUrl;
        }

        // 分批模式的参数配置
        Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
        // 是否启用分批模式
        boolean isBatch = batchConfig.get("ENABLE") != null ? (boolean) batchConfig.get("ENABLE") : false;
        // 分批的请求参数
        List<Map<String, Object>> batchParamList = (List<Map<String, Object>>) batchConfig.get("PARAMS");
        // 分批的间隔
        Integer interval = (Integer) batchConfig.get("INTERVAL");

        // json列表
        List<String> originJsonList = CollectionUtil.newArrayList();
        // 数据json列表
        List<String> dataJsonList = CollectionUtil.newArrayList();

        // 分批模式 记录上一次数据，用于对比两次数据，若重复 则结束，避免死循环
        long lastJsonHash = -1L;

        // 循环计数器，超过最大数则结束，避免死循环
        int loopCount = 0;

        do {
            // API的请求参数
            Map<String, String> reqParams = MyDataUtil.parseToKvMapObj(api.getReqParams());

            // 若启用分批，则将分批参数加入请求参数中
            if (isBatch) {
                Map<String, String> batchParams = jobBatchService.parseToMap(batchParamList);
                reqParams = MapUtil.union(reqParams, batchParams);
            }

            // 调用接口 获取json
            String originJsonString = HttpUtil.send(api.getApiMethod(), apiUrl, reqParams, null, null, null);

            // json为空则结束
            if (StringUtil.isEmpty(originJsonString)) {
                break;
            }

            // 对比上一次数据
            if (lastJsonHash != -1L) {
                if (lastJsonHash == HashUtil.mixHash(originJsonString)) {
                    break;
                }
            }
            // 记录最新json的hash
            lastJsonHash = HashUtil.mixHash(originJsonString);

            // json字符串转为json对象
            JSON originJson = JsonUtil.parse(originJsonString);

            // 提取业务数据json对象
            JSON dataJson = (JSON) originJson.getByPath(StringUtil.nullToEmpty(api.getFieldPrefix()));
            // 若没有数据，则结束
            if (dataJson instanceof JSONObject && ((JSONObject) dataJson).isEmpty()) {
                break;
            } else if (dataJson instanceof JSONArray && ((JSONArray) dataJson).isEmpty()) {
                break;
            }

            // 将json加入列表，待后续处理
            originJsonList.add(originJsonString);

            // 数据json加入列表
            dataJsonList.add(dataJson.toString());

            if (isBatch) {
                // 分批模式
                // 调整递增参数值
                jobBatchService.incBatchParam(batchParamList);
                // 暂停间隔
                ThreadUtil.sleep(interval, TimeUnit.SECONDS);
            }

            loopCount++;
            if (loopCount > 100) {
                break;
            }

        } while (isBatch);

//        handleJson(originJsonList, apiPrefix, jobContextData);
        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String originJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_ORIGIN_JSON);
        if (StringUtil.isNotEmpty(originJsonKey)) {
            jobContextData.put(originJsonKey, originJsonList);
        }
        String dataJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_DATA_JSON);
        if (StringUtil.isNotEmpty(dataJsonKey)) {
            jobContextData.put(dataJsonKey, dataJsonList);
        }
    }
}