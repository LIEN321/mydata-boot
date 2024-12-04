package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
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
    public PushDataToApi(PipelineTask pipelineTask) {
        super(pipelineTask);
    }

    @Override
    public void execute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();

        Map<String, String> inputMap = getInputMap();
        String bizDataKey = inputMap.get(MyDataConstant.JOB_DATA_KEY_BIZ_DATA);
        if (StringUtil.isEmpty(bizDataKey)) {
            log.info("{} 没有配置业务数据变量，无法获取业务数据，结束执行。", pipelineTask.getTaskName());
            return;
        }

        // 获取业务数据
        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(bizDataKey);
        log.info("从job上下文获取的业务数据：{}", bizDataList);
        if (CollectionUtil.isEmpty(bizDataList)) {
            log.info("{} 没有获取有效业务数据，结束执行。", pipelineTask.getTaskName());
            return;
        }

        // 字段映射
        Map<String, String> fieldMapping = getFieldMapping();

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
            Map<String, String> reqForms = null;
            String reqBody = null;

            String reqBodyType = api.getReqBodyType();
            if (MyDataConstant.API_REQUEST_BODY_TYPE_FORM.equals(reqBodyType)) {
                // TODO 设置reqForms

            } else if (MyDataConstant.API_REQUEST_BODY_TYPE_JSON.equals(reqBodyType)) {

                // 分批模式的参数配置
                Map<String, Object> batchConfig = (Map<String, Object>) pipelineTask.getTaskConfig().get("BATCH");
                // 是否启用分批模式
                boolean isBatch = batchConfig.get("ENABLE") != null ? (boolean) batchConfig.get("ENABLE") : false;
                // 分批的间隔
                Integer interval = (Integer) batchConfig.get("INTERVAL");
                // 分批的批次数量
                Integer count = (Integer) batchConfig.get("COUNT");

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
                    } else {
                        // 不分批，则发送所有数据
                        jsonArray.addAll(apiDataList);
                    }

                    // api中的原始body
                    reqBody = api.getReqBodyRaw();

                    // 将json字符串 替换${data}占位符
                    reqBody = StringUtil.substitute(reqBody, MyDataConstant.JOB_DATA_KEY_BIZ_DATA, jsonArray.toString());

                    // 调用api
                    HttpUtil.send(api.getApiMethod(), apiUrl, null, null, reqForms, reqBody);

                    if (isBatch) {
                        // 暂停间隔
                        ThreadUtil.sleep(interval, TimeUnit.SECONDS);
                    }
                } while (isBatch);

            }
            log.info("向接口发送数据：{}", reqBody);
        } else {
            // TODO 单数据模式，逐个调API推送数据
        }
    }
}
