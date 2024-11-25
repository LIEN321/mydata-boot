package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.cache.MdCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MdConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.thread.ThreadUtil;
import tech.zhiwei.tool.util.RandomUtil;

import java.util.List;
import java.util.Map;

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
        // TODO 临时延长时间，模拟长时间执行过程
        ThreadUtil.sleep(RandomUtil.randomInt(20) * 1000L);
        
        PipelineTask pipelineTask = getPipelineTask();

        // 获取业务数据
        List<Map<String, Object>> bizDataList = (List<Map<String, Object>>) jobContextData.get(MdConstant.JOB_DATA_KEY_BIZ_DATA);
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
        App app = MdCache.getApp(pipelineTask.getAppId());
        String apiPrefix = app.getApiPrefix();

        // 获取接口信息
        AppApi api = MdCache.getApi(pipelineTask.getApiId());
        String apiUrl = api.getApiUri();
        if (StringUtil.isNotEmpty(apiPrefix)) {
            apiUrl = apiPrefix + apiUrl;
        }

        // 多数据模式，批量推送
        if (MdConstant.API_DATA_MODE_LIST == api.getDataMode()) {
            Map<String, String> reqForms = null;
            String reqBody = null;

            String reqBodyType = api.getReqBodyType();
            if (MdConstant.API_REQUEST_BODY_TYPE_FORM.equals(reqBodyType)) {
                // TODO 设置reqForms

            } else if (MdConstant.API_REQUEST_BODY_TYPE_JSON.equals(reqBodyType)) {
                // apiDataList 转为json字符串
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(apiDataList);

                // api中的原始body
                reqBody = api.getReqBodyRaw();

                // 将json字符串 替换${data}占位符
                reqBody = StringUtil.substitute(reqBody, MdConstant.JOB_DATA_KEY_BIZ_DATA, jsonArray.toString());
            }
            HttpUtil.send(api.getApiMethod(), apiUrl, null, null, reqForms, reqBody);
            log.info("向接口发送数据：{}", reqBody);
            // TODO 分批模式
        } else {
            // TODO 单数据模式，逐个调API推送数据
        }
    }
}
