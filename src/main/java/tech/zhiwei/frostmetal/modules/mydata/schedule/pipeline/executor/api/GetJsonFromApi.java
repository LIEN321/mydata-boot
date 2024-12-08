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
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
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
 * 从API获取JSON
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Slf4j
public class GetJsonFromApi extends TaskExecutor {

    private final JobBatchService jobBatchService = SpringUtil.getBean(JobBatchService.class);

    public GetJsonFromApi(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        // 获取应用信息
        App app = MyDataCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        log("将调用应用 {} 的接口", app.getAppName());

        // 获取接口信息
        AppApi api = MyDataCache.getApi(pipelineTask.getApiId());
        final String apiUrl = StringUtil.emptyIfNull(apiPrefix) + api.getApiUri();

        log("接口地址：{}", apiUrl);

        // 分批模式的参数配置
        Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
        // 是否启用分批模式
        boolean isBatch = batchConfig.get("ENABLE") != null ? (boolean) batchConfig.get("ENABLE") : false;
        // 分批的请求参数
        List<Map<String, Object>> batchParamList = (List<Map<String, Object>>) batchConfig.get("PARAMS");
        // 分批的间隔
        Integer interval = (Integer) batchConfig.get("INTERVAL");

        log("分批模式配置：{}", batchConfig);

        // json列表
        List<String> originJsonList = CollectionUtil.newArrayList();
        // 数据json列表
        List<String> dataJsonList = CollectionUtil.newArrayList();

        // 分批模式 记录上一次数据，用于对比两次数据，若重复 则结束，避免死循环
        long lastJsonHash = -1L;

        // 循环计数器，超过最大数则结束，避免死循环
        int loopCount = 1;

        do {
            loopCount++;
            if (loopCount > 100) {
                error("执行次数超过上限{}，结束执行！", 100);
                break;
            }

            // API的请求参数
            Map<String, String> reqParams = MyDataUtil.parseToKvMapObj(api.getReqParams());
            Map<String, String> reqHeaders = MyDataUtil.parseToKvMapObj(api.getReqHeaders());
            Map<String, String> reqForm = null;
            String reqBody = null;
            // 根据请求体类型 初始对应的数据
            if (MyDataConstant.API_REQUEST_BODY_TYPE_FORM.equals(api.getReqBodyType())) {
                reqForm = MyDataUtil.parseToKvMapObj(api.getReqBodyForm());
            } else {
                reqBody = api.getReqBodyRaw();
            }

            // 若启用分批，则将分批参数加入请求参数中
            if (isBatch) {
                Map<String, String> batchParams = jobBatchService.parseToMap(batchParamList);
                reqParams = MapUtil.union(reqParams, batchParams);
            }


            log("第{}次调用接口 [{}] {}", loopCount, api.getApiMethod(), apiUrl);
            log("\trequest param：{}", reqParams);
            log("\trequest header：{}", reqHeaders);
            log("\trequest form：{}", reqForm);
            log("\trequest body：{}", reqBody);

            // 调用接口 获取json
            String originJsonString = HttpUtil.send(api.getApiMethod(), apiUrl, reqParams, reqHeaders, reqForm, reqBody);
            log("\t返回JSON：{}", originJsonString);

            // json为空则结束
            if (StringUtil.isEmpty(originJsonString)) {
                error("JSON为空字符串，结束执行。");
                break;
            }

            // 对比上一次数据
            if (lastJsonHash != -1L) {
                if (lastJsonHash == HashUtil.mixHash(originJsonString)) {
                    error("本次结果与前一次 完全一样，结束执行。");
                    break;
                }
            }
            // 记录最新json的hash
            lastJsonHash = HashUtil.mixHash(originJsonString);

            // json字符串转为json对象
            JSON originJson = JsonUtil.parse(originJsonString);

            // 提取业务数据json对象
            String fieldPrefix = StringUtil.nullToEmpty(api.getFieldPrefix());
            JSON dataJson;
            if (StringUtil.isEmpty(fieldPrefix)) {
                dataJson = originJson;
            } else {
                dataJson = (JSON) originJson.getByPath(fieldPrefix);
            }

            log("数据所在层级：{}，提取的数据JSON：{}", fieldPrefix, dataJson);

            // 若没有数据，则结束
            if (dataJson instanceof JSONObject && ((JSONObject) dataJson).isEmpty()) {
                error("JSON为空 {}，结束执行。", dataJson.toString());
                break;
            } else if (dataJson instanceof JSONArray && ((JSONArray) dataJson).isEmpty()) {
                error("JSON为空 {}，结束执行。", dataJson.toString());
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
                log("分批模式，调整分批参数：{}", batchParamList);

                // 暂停间隔
                ThreadUtil.sleep(interval, TimeUnit.SECONDS);
                log("分批模式，等待 {} 秒", interval);
            }
        } while (isBatch);

//        handleJson(originJsonList, apiPrefix, jobContextData);
        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String originJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_ORIGIN_JSON);
        if (StringUtil.isNotEmpty(originJsonKey)) {
            jobContextData.put(originJsonKey, originJsonList);
        }
        log("获取的原始JSON：{}", originJsonList);

        String dataJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_DATA_JSON);
        if (StringUtil.isNotEmpty(dataJsonKey)) {
            jobContextData.put(dataJsonKey, dataJsonList);
        }
        log("获取的数据JSON：{}", dataJsonList);
    }
}