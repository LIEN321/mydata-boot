package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.DataField;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineBizData;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineJson;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobApiService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobBatchService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobJsonService;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.thread.ThreadUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 从API获取JSON
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Slf4j
public class GetJsonFromApi extends TaskExecutor {

    public GetJsonFromApi(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        // TODO 增加输入变量：业务数据
        // 输入参数
        Map<String, String> inputMap = getInputMap();
        // 获业务数据的key
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        PipelineBizData pipelineBizData = null;
        if (StringUtil.isNotEmpty(bizDataKey)) {
            // 上下文业务数据
            pipelineBizData = (PipelineBizData) jobContextData.get(bizDataKey);
        }

        // 获取应用信息
        App app = MyDataCache.getApp(pipelineTask.getAppId());

        log("将调用应用 {} 的接口", app.getAppName());

        // 获取接口信息
        AppApi api = MyDataCache.getApi(pipelineTask.getApiId());
        // 业务数据在json的路径
        String fieldPrefix = StringUtil.nullToEmpty(api.getFieldPrefix());

//        log("接口地址：{}", apiUrl);

        // 分批模式的参数配置
        Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
        // 是否启用分批模式
        boolean isBatch = batchConfig.get("ENABLE") != null && (boolean) batchConfig.get("ENABLE");
        // 分批的请求参数
        List<Map<String, Object>> batchParamList = (List<Map<String, Object>>) batchConfig.get("PARAMS");
        // 分批的间隔
        Integer interval = (Integer) batchConfig.get("INTERVAL");

        log("分批模式配置：{}", batchConfig);

        // 流水线的json
        List<PipelineJson> pipelineJsons = CollectionUtil.newArrayList();

        // 分批模式 记录上一次数据，用于对比两次数据，若重复 则结束，避免死循环
        long lastJsonHash = -1L;

        // 循环计数器，超过最大数则结束，避免死循环
        int loopCount = 0;

        do {
            loopCount++;
            if (loopCount > 100) {
                error("执行次数超过上限{}，结束执行！", 100);
                break;
            }

            Map<String, String> batchParams = null;
            // 若启用分批，则将分批参数加入请求参数中
            if (isBatch) {
                batchParams = JobBatchService.parseToMap(batchParamList);
            }

            Map<String, Object> bizDataMap = MapUtil.newHashMap();
            Map<String, String> fieldTypeMapping = null;
            if (pipelineBizData != null) {
                if (CollectionUtil.isEmpty(pipelineBizData.getBizData())) {
                    log("没有业务数据可作为参数，结束执行");
                    return;
                } else {
                    // 字段编号-字段类型
                    fieldTypeMapping = pipelineBizData.getDataFields().stream().collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                    bizDataMap.putAll(pipelineBizData.getBizData().get(0));
                }
            }

            log("第{}次调用接口", loopCount);

            // 调用接口 获取json
            String originJsonString = JobApiService.callApi(this, app, api, batchParams, bizDataMap, fieldTypeMapping);
            log("\t获得JSON：{}", originJsonString);

            // json为空则结束
            if (JsonUtil.isEmpty(originJsonString)) {
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

            // 将json字符串转为流水线json对象
            pipelineJsons.addAll(JobJsonService.pipelineJson(originJsonString, fieldPrefix));

            if (isBatch) {
                // 分批模式
                // 调整递增参数值
                JobBatchService.incBatchParam(batchParamList);
                log("分批模式，调整分批参数：{}", batchParamList);

                // 暂停间隔
                ThreadUtil.sleep(interval, TimeUnit.SECONDS);
                log("分批模式，等待 {} 秒", interval);
            }
        } while (isBatch);

        // 将结果保存到 job上下文
        Map<String, String> output = getOutputMap();
        String pipelineJsonKey = output.get(MyDataConstant.JOB_DATA_KEY_PIPELINE_JSON);
        if (StringUtil.isNotEmpty(pipelineJsonKey)) {
            jobContextData.put(pipelineJsonKey, pipelineJsons);
        }
        log("从API获取JSON完成");
    }
}