package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.frostmetal.core.base.common.ResponseCode;
import tech.zhiwei.frostmetal.modules.mydata.cache.MyDataCache;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineJob;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApiResponse;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApp;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobApiService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobVarService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.util.NumberUtil;

import java.util.Map;

/**
 * 流水线任务执行器
 *
 * @author LIEN
 * @since 2024/11/21
 */
@Getter
@Slf4j
public abstract class ApiTaskExecutor extends TaskExecutor {

    public static final String AUTH_CONFIG_API = "api";
    public static final String AUTH_CONFIG_KEY = "key";
    public static final String AUTH_CONFIG_VALUE = "value";
    public static final String AUTH_CONFIG_ADD_TO = "addTo";

    /**
     * APP认证类型：jwt
     */
    public static final String APP_AUTH_TYPE_JWT = "jwt";
    public static final String JWT_API = "api";
    public static final String JWT_ADD_TO = "addTo";
    public static final String JWT_HEADER_PREFIX = "prefix";
    public static final String JWT_HEADER_KEY = "key";
    public static final String JWT_QUERY_PARAM = "param";
    /**
     * APP认证类型：cookie
     */
    public static final String APP_AUTH_TYPE_COOKIE = "cookie";
    /**
     * APP认证类型：API Key
     */
    public static final String APP_AUTH_TYPE_API_KEY = "api_key";

    /**
     * 已完成授权的App
     */
    private Map<Long, PipelineApp> authedApps;

    public ApiTaskExecutor(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        authedApps = (Map<Long, PipelineApp>) jobContextData.get(PipelineJob.PIPELINE_PARAM_KEY_AUTHED_APP);
    }

    /**
     * 流水线任务 调用API
     *
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
    public PipelineApiResponse callApi(String method, String url, Map<String, String> reqHeaders, Map<String, String> queryParams, Map<String, String> reqForm, String reqBody, Map<String, Object> bizData, Map<String, Object> pipelineVars) {
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

        this.log("\trequest url: [{}] {}", method, url);
        this.log("\trequest param : {}", queryParams);
        this.log("\trequest header : {}", reqHeaders);
        this.log("\trequest form : {}", reqForm);
        this.log("\trequest body : {}", reqBody);

        // 发送请求，获取响应结果
        try (HttpResponse response = HttpUtil.send(method, url, queryParams, reqHeaders, reqForm, reqBody);) {
            String cookie = response.getCookieStr();
            String responseBody = response.body();
            this.log("\tresponse status : {}", response.getStatus());
            this.log("\tresponse body : {}", responseBody);

            return new PipelineApiResponse(response.getStatus(), responseBody, cookie);
        }
    }

    /**
     * 执行app认证
     *
     * @param app 待执行的app
     */
    public PipelineApp doAppAuth(App app) {
        AssertUtil.notNull(app, "应用认证失败：待认证的应用无效");

        PipelineApp pipelineApp = BeanUtil.copyProperties(app, PipelineApp.class, "reqHeaders");
        pipelineApp.setReqHeaders(ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(app.getReqHeaders())));

        // 认证类型
        String authType = app.getAuthType();
        if (StringUtil.isEmpty(authType)) {
            log("应用{} 无需认证...", app.getAppName());
            return pipelineApp;
        }

        // 检查是否已认证过
        if (authedApps.containsKey(app.getId())) {
            log("应用已在流水线中认证，无需重复认证");
            return authedApps.get(app.getId());
        }

        // 认证配置
        Map<String, Object> authConfig = app.getAuthConfig();
        // app headers
        Map<String, String> appHeaders = pipelineApp.getReqHeaders();
        // app query params
        Map<String, Object> appQueryParams = pipelineApp.getQueryParams();

        log("应用{} 认证开始，认证方式为{}", app.getAppName(), app.getAuthType());
        // jwt 认证
        if (APP_AUTH_TYPE_JWT.equals(authType)) {
            Map<String, Object> jwtConfig = (Map<String, Object>) authConfig.get("jwt");
            AssertUtil.notNull(jwtConfig, "JWT配置无效，请确认");

            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) jwtConfig.get(AUTH_CONFIG_API));
            // 认证接口
            AppApi api = MyDataCache.getApi(apiId);

            // api headers
            Map<String, String> apiHeaders = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqHeaders()));
            // 合并headers
            Map<String, String> reqHeaders = ObjectUtil.cloneByStream(MapUtil.union(appHeaders, apiHeaders));

            // query params
            Map<String, String> apiReqParams = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqParams()));

            // request form
            Map<String, String> reqForm = null;
            // request body
            String reqBody = null;

            // 根据请求体类型 初始对应的数据
            if (MyDataConstant.API_REQUEST_BODY_TYPE_FORM.equals(api.getReqBodyType())) {
                reqForm = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqBodyForm()));
            } else {
                reqBody = ObjectUtil.cloneByStream(api.getReqBodyRaw());
            }

            // 调用认证接口
            PipelineApiResponse apiResponse = callApi(api.getApiMethod(), StringUtil.emptyIfNull(app.getApiPrefix()) + api.getApiUri(), reqHeaders, apiReqParams, reqForm, reqBody, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());

            String responseData = apiResponse.getData();
            String token = responseData;
            String apiFieldPrefix = api.getFieldPrefix();
            if (StringUtil.isNotEmpty(apiFieldPrefix)) {
                log("从 {} 中的 {} 提取token", responseData, apiFieldPrefix);
                // 获取接口返回的json
                JSON json = JsonUtil.parse(responseData);
                // json中提取token
                token = (String) json.getByPath(apiFieldPrefix);
            } else {
                log("API 未配置层级，使用响应的全部内容作为token");
            }
            log("token={}", token);

            // add to
            String addTo = (String) jwtConfig.get(AUTH_CONFIG_ADD_TO);
            // header
            if (MyDataConstant.HTTP_HEADER.equals(addTo)) {
                log("token 添加到 header");
                String key = (String) jwtConfig.get(JWT_HEADER_KEY);
                String prefix = (String) jwtConfig.get(JWT_HEADER_PREFIX);
                prefix = StringUtil.isNotEmpty(prefix) ? prefix + " " : "";
                String value = prefix + token;
                appHeaders.put(key, value);
            } else if (MyDataConstant.HTTP_QUERY.equals(addTo)) {
                log("token 添加到 query param");
                String paramName = (String) jwtConfig.get(JWT_QUERY_PARAM);
                appQueryParams.put(paramName, token);
            }
        }
        // cookie 认证
        else if (APP_AUTH_TYPE_COOKIE.equals(authType)) {
            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) authConfig.get(AUTH_CONFIG_API));
            // 认证接口
            AppApi api = MyDataCache.getApi(apiId);

            PipelineApiResponse apiResponse = JobApiService.callApi(this, pipelineApp, api, null, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());

            String cookie = apiResponse.getCookie();
            if (StringUtil.isNotEmpty(cookie)) {
                appHeaders.put("Cookie", cookie);
            }
        }
        // api key 认证
        else if (APP_AUTH_TYPE_API_KEY.equals(authType)) {
            // key
            String key = (String) authConfig.get(ApiTaskExecutor.AUTH_CONFIG_KEY);
            // value
            String value = (String) authConfig.get(ApiTaskExecutor.AUTH_CONFIG_VALUE);
            // add to
            String addTo = (String) authConfig.get(ApiTaskExecutor.AUTH_CONFIG_ADD_TO);

            if (MyDataConstant.HTTP_HEADER.equals(addTo)) {
                appHeaders.put(key, value);
            } else {
                appQueryParams.put(key, value);
            }
        }

        authedApps.put(app.getId(), pipelineApp);

        log("应用{} 认证成功！", app.getAppName());
        return pipelineApp;
    }
}
