package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.thread.ThreadUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 向API发送数据
 *
 * @author LIEN
 * @since 2024/11/22
 */
@Slf4j
public class PushDataToApi extends TaskExecutor {
    public PushDataToApi(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        Map<String, String> inputMap = getInputMap();
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            error("{} 没有配置业务数据变量，无法获取业务数据，结束执行。", pipelineTask.getTaskName());
            return;
        }

        // 获取业务数据
        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(bizDataKey);
        log("从上下文获取的业务数据：{}", bizDataList);
        if (CollectionUtil.isEmpty(bizDataList)) {
            error("没有业务数据，结束执行。");
            return;
        }

        // 字段映射
        Map<String, String> fieldMapping = getFieldMapping();
        log("字段映射配置：{}", fieldMapping);

        // 根据字段映射 转换为接口结构的数据
        List<Map<String, Object>> apiDataList = CollectionUtil.newArrayList();
        bizDataList.forEach(bizData -> {
            Map<String, Object> apiData = MapUtil.newHashMap();
            // 根据映射关系 将数据转换为api的数据结构
            fieldMapping.forEach((standardCode, apiCode) -> {
                // 若字段映射中 未设置api参数名，则跳过处理；
                if (StrUtil.isEmpty(apiCode)) {
                    return;
                }
                apiData.put(apiCode, bizData.get(standardCode));
            });

            apiDataList.add(apiData);
        });

        log("转换后的业务数据：", apiDataList);

        // 获取应用信息
        App app = MyDataCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        // 获取接口信息
        AppApi api = MyDataCache.getApi(pipelineTask.getApiId());
        String apiUrl = api.getApiUri();
        if (StringUtil.isNotEmpty(apiPrefix)) {
            apiUrl = apiPrefix + apiUrl;
        }

        // 多数据模式，批量推送
        if (MyDataConstant.API_DATA_MODE_LIST == api.getDataMode()) {
            String reqBody = null;

            String reqBodyType = api.getReqBodyType();
            if (MyDataConstant.API_REQUEST_BODY_TYPE_FORM.equals(reqBodyType)) {
                // TODO 暂不支持form模式发送多条数据
                error("暂不支持form模式发送多条数据");
                throw new IllegalArgumentException("接口{}的请求体类型是form，暂不支持form模式发送多条数据。");
            } else if (MyDataConstant.API_REQUEST_BODY_TYPE_JSON.equals(reqBodyType)) {
                // 分批模式的参数配置
                Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
                // 是否启用分批模式
                boolean isBatch = batchConfig.get("ENABLE") != null ? (boolean) batchConfig.get("ENABLE") : false;
                // 分批的间隔
                Integer interval = (Integer) batchConfig.get("INTERVAL");
                // 分批的批次数量
                Integer count = (Integer) batchConfig.get("COUNT");

                log("分批模式配置：{}", batchConfig);

                // 分批执行次数
                int batchRound = 0;

                do {
                    // json字符串
                    JSONArray jsonArray = new JSONArray();
                    if (isBatch) {
                        // 从数据列表中 提取分批的数据
                        List<Map<String, Object>> subDataList = CollectionUtil.sub(apiDataList, batchRound * count, (batchRound + 1) * count);
                        // 分批数据为空，则结束
                        if (CollectionUtil.isEmpty(subDataList)) {
                            break;
                        }
                        jsonArray.addAll(subDataList);

                        // 执行次数+1
                        batchRound++;

                        log("分批模式，第{}批数据：{}", batchRound, subDataList);
                    } else {
                        // 不分批，则发送所有数据
                        jsonArray.addAll(apiDataList);

                        log("不分批，全部数据：{}", batchRound, apiDataList);
                    }

                    // api中的原始body
                    reqBody = api.getReqBodyRaw();

                    // 将json字符串 替换${data}占位符
                    reqBody = StringUtil.substitute(reqBody, MyDataConstant.JOB_DATA_KEY_BIZ_DATA, jsonArray.toString());

                    log("调用接口 [{}] {}", api.getApiMethod(), apiUrl);
                    log("\t请求体：{}", reqBody);
                    // 调用api
                    // TODO params headers
                    HttpUtil.send(api.getApiMethod(), apiUrl, null, null, null, reqBody);
                    log("调用接口成功");

                    if (isBatch) {
                        // 暂停间隔
                        ThreadUtil.sleep(interval, TimeUnit.SECONDS);
                        log("分批模式，等待 {} 秒", interval);
                    }
                } while (isBatch);
            }
        } else {
            // TODO 单数据模式，逐个调API推送数据
        }
    }
}
