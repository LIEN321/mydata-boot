package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApiResponse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApp;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.util.Map;

/**
 * 任务调用API的服务类
 *
 * @author LIEN
 * @since 2024/12/16
 */
@Slf4j
public class JobApiService {

    /**
     * 流水线任务 调用API
     *
     * @param taskExecutor 任务
     * @param method       http method
     * @param url          http url
     * @param reqHeaders   request headers
     * @param queryParams  request params
     * @param reqForm      request form
     * @param reqBody      request body
     * @param bizData      业务数据，用于替换API定义中的${var}变量
     * @param pipelineVars 流水线上下文变量，用于替换API定义中的${var}变量
     * @return 流水线API响应
     */
    public static PipelineApiResponse callApi(TaskExecutor taskExecutor, String method, String url, Map<String, String> reqHeaders, Map<String, String> queryParams, Map<String, String> reqForm, String reqBody, Map<String, Object> bizData, Map<String, Object> pipelineVars) {
        // 解析替换系统变量值
        url = JobVarService.processSysVarValue(url);
        JobVarService.processSysVarValues(reqHeaders);
        JobVarService.processSysVarValues(queryParams);
        JobVarService.processSysVarValues(reqForm);
        reqBody = JobVarService.processSysVarValue(reqBody);

        // 替换业务数据变量值
        try {
            JobVarService.processDataFieldVar(queryParams, bizData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Param中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqHeaders, bizData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Header中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqForm, bizData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Form中的参数出错，原因：" + e.getMessage());
        }
        try {
            reqBody = JobVarService.processDataFieldVar(reqBody, bizData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Body中的参数出错，原因：" + e.getMessage());
        }

        taskExecutor.log("\trequest url: [{}] {}", method, url);
        taskExecutor.log("\trequest param : {}", queryParams);
        taskExecutor.log("\trequest header : {}", reqHeaders);
        taskExecutor.log("\trequest form : {}", reqForm);
        taskExecutor.log("\trequest body : {}", reqBody);

        // 发送请求，获取响应结果
        try (HttpResponse response = HttpUtil.send(method, url, queryParams, reqHeaders, reqForm, reqBody);) {
            String cookie = response.getCookieStr();
            String responseBody = response.body();
            taskExecutor.log("\tresponse status : {}", response.getStatus());
            taskExecutor.log("\tresponse body : {}", responseBody);

            return new PipelineApiResponse(response.getStatus(), responseBody, cookie);
        }
    }

    /**
     * 调用应用的接口
     *
     * @param app 应用
     * @param api 接口
     * @return 接口响应内容
     */
    public static PipelineApiResponse callApi(TaskExecutor taskExecutor, PipelineApp app, AppApi api, Map<String, String> batchParams, Map<String, Object> bizData, Map<String, String> fieldTypeMapping) {
        AssertUtil.notNull(app);
        AssertUtil.notNull(api);

        // API的请求参数
        Map<String, String> reqParams = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqParams()));
        if (MapUtil.isNotEmpty(batchParams)) {
            reqParams = MapUtil.union(reqParams, batchParams);
        }

        // APP 全局Header
        Map<String, String> appHeaders = app.getReqHeaders();
        if (ObjectUtil.isNull(appHeaders)) {
            appHeaders = MapUtil.newHashMap();
        }
        // APP 全局Query param
        Map<String, Object> appQueryParams = app.getQueryParams();
        // API 请求Header
        Map<String, String> apiHeaders = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqHeaders()));
        // API Header 并入 全局Header
        Map<String, String> reqHeaders = ObjectUtil.cloneByStream(MapUtil.union(appHeaders, apiHeaders));

        Map<String, String> reqForm = null;
        String reqBody = null;

        // 根据请求体类型 初始对应的数据
        if (MyDataConstant.API_REQUEST_BODY_TYPE_FORM.equals(api.getReqBodyType())) {
            reqForm = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqBodyForm()));
        } else {
            reqBody = ObjectUtil.cloneByStream(api.getReqBodyRaw());
            if (MapUtil.isNotEmpty(bizData)) {
                reqBody = StringUtil.substitute(reqBody, bizData);
            }
        }

        // 应用统一的api地址前缀
        String apiPrefix = app.getApiPrefix();
        // api地址
        String apiUrl = StringUtil.emptyIfNull(apiPrefix) + api.getApiUri();

        // 解析替换业务数据变量
        apiUrl = JobVarService.processDataFieldVar(apiUrl, bizData, fieldTypeMapping);
        apiUrl = HttpUtil.urlWithForm(apiUrl, appQueryParams, null, true);

        // 解析替换系统变量值
        apiUrl = JobVarService.processSysVarValue(apiUrl);
        JobVarService.processSysVarValues(reqParams);
        JobVarService.processSysVarValues(reqHeaders);
        JobVarService.processSysVarValues(reqForm);
        reqBody = JobVarService.processSysVarValue(reqBody);

        try {
            JobVarService.processDataFieldVar(reqParams, bizData, fieldTypeMapping);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Param中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqHeaders, bizData, fieldTypeMapping);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Header中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqForm, bizData, fieldTypeMapping);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Form中的参数出错，原因：" + e.getMessage());
        }
        try {
            reqBody = JobVarService.processDataFieldVar(reqBody, bizData, fieldTypeMapping);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Body中的参数出错，原因：" + e.getMessage());
        }

        taskExecutor.log("\trequest url: [{}] {}", api.getApiMethod(), apiUrl);
        taskExecutor.log("\trequest param : {}", reqParams);
        taskExecutor.log("\trequest header : {}", reqHeaders);
        taskExecutor.log("\trequest form : {}", reqForm);
        taskExecutor.log("\trequest body : {}", reqBody);

        // 发送请求，获取响应结果
        try (HttpResponse response = HttpUtil.send(api.getApiMethod(), apiUrl, reqParams, reqHeaders, reqForm, reqBody);) {
            String cookie = response.getCookieStr();
            String responseBody = response.body();
            taskExecutor.log("\tresponse status : {}", response.getStatus());
            taskExecutor.log("\tresponse body : {}", responseBody);

            return new PipelineApiResponse(response.getStatus(), responseBody, cookie);
        }
    }
}
