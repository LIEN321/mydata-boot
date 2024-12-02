package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.json.JSON;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Map;

/**
 * 从API获取json
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Slf4j
public class GetJsonFromApi extends TaskExecutor {

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

        // TODO 分批模式
        Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
        boolean isBatch = batchConfig.get("ENABLE") != null ? (boolean) batchConfig.get("ENABLE") : false;
        Map<String, Object> batchParams = (Map<String, Object>) batchConfig.get("PARAMS");
        Integer interval = (Integer) batchConfig.get("INTERVAL");
        Integer endType = (Integer) batchConfig.get("END_TYPE");

        do {
            // API的请求参数
            Map<String, String> reqParams = MyDataUtil.parseToKvMapObj(api.getReqParams());

            // 若启用分批，则将分批参数加入请求参数中
            if(isBatch){
                reqParams = MapUtil.u
            }

        } while (isBatch);

        // 调用api，获取原始json字符串
        String originJsonString = HttpUtil.send(api.getApiMethod(), apiUrl, null, null, null, null);
        log.info("API获取JSON：{}", originJsonString);

        handleJson(originJsonString, apiPrefix, jobContextData);
    }

    /**
     * 将json字符串转为业务数据
     *
     * @param jsonString     json字符串
     * @param fieldPrefix    业务数据在json中的字段前缀
     * @param jobContextData job上下文数据
     */
    protected void handleJson(String jsonString, String fieldPrefix, Map<String, Object> jobContextData) {
        // json字符串转为json对象
        JSON originJson = JsonUtil.parse(jsonString);

        // 提取业务数据json对象
        JSON dataJson = (JSON) originJson.getByPath(fieldPrefix);

        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String originJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_ORIGIN_JSON);
        if (StringUtil.isNotEmpty(originJsonKey)) {
            jobContextData.put(originJsonKey, jsonString);
        }
        String dataJsonKey = output.get(MyDataConstant.TASK_DATA_KEY_DATA_JSON);
        if (StringUtil.isNotEmpty(dataJsonKey)) {
            jobContextData.put(dataJsonKey, dataJson.toString());
        }
    }
}