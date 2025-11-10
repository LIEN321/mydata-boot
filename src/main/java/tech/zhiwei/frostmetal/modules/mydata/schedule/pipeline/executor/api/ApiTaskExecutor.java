package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.api;

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
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApiResponseConfig;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.bean.PipelineApp;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobVarService;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.bean.BeanUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.json.JsonUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ExceptionUtil;
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

    /**
     * APP认证类型：jwt
     */
    public static final String APP_AUTH_TYPE_JWT = "jwt";
    public static final String JWT_CONFIG_API = "api";
    public static final String JWT_CONFIG_ADD_TO = "addTo";
    public static final String JWT_CONFIG_HEADER_PREFIX = "prefix";
    public static final String JWT_CONFIG_HEADER_KEY = "key";
    public static final String JWT_CONFIG_QUERY_PARAM = "param";
    /**
     * APP认证类型：cookie
     */
    public static final String APP_AUTH_TYPE_COOKIE = "cookie";
    public static final String COOKIE_CONFIG_API = "api";
    /**
     * APP认证类型：API Key
     */
    public static final String APP_AUTH_TYPE_API_KEY = "api_key";
    public static final String API_KEY_CONFIG_ADD_TO = "addTo";
    public static final String API_KEY_CONFIG_KEY = "key";
    public static final String API_KEY_CONFIG_VALUE = "value";

    /**
     * 已完成授权的App
     */
    private Map<Long, PipelineApp> authedApps;

    /**
     * 流水线上下文
     */
    private Map<String, Object> jobContextData;

    public ApiTaskExecutor(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        this.jobContextData = jobContextData;
        authedApps = (Map<Long, PipelineApp>) jobContextData.get(PipelineJob.PIPELINE_PARAM_KEY_AUTHED_APP);
    }

    /**
     * 根据应用和API信息，提取调用api的所需参数，再调用 {@link #callApi(String, String, Map, Map, Map, String, Map)} 统一调用API
     *
     * @param app         应用
     * @param api         API
     * @param batchParams 分批参数
     * @param bizData     业务数据，用于替换API定义中的${var}变量
     * @return 流水线API响应
     */
    public PipelineApiResponse callApi(PipelineApp app, AppApi api, Map<String, String> batchParams, Map<String, Object> bizData) {
        AssertUtil.notNull(app);
        AssertUtil.notNull(api);

        // method
        String method = api.getApiMethod();

        // url
        String apiPrefix = StringUtil.emptyIfNull(app.getApiPrefix());
        String url = apiPrefix + api.getApiUri();

        // header
        // app headers
        Map<String, String> appHeaders = app.getReqHeaders();
        // api headers
        Map<String, String> apiHeaders = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqHeaders()));
        // 合并headers
        Map<String, String> reqHeaders = ObjectUtil.cloneByStream(MapUtil.union(appHeaders, apiHeaders));

        // query params
        Map<String, String> queryParams = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqParams()));
        if (MapUtil.isNotEmpty(batchParams)) {
            queryParams = MapUtil.union(queryParams, batchParams);
        }

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

        // 调用API，获取响应内容
        PipelineApiResponse apiResponse = callApi(method, url, reqHeaders, queryParams, reqForm, reqBody, bizData);

        // 根据配置的规则，校验响应是否成功
        // 获取API的响应配置
        Map<String, Object> respConfigMap = api.getRespConfig();
        // 获取响应配置模式，默认复用应用的配置
        String mode = MapUtil.getStr(respConfigMap, MyDataConstant.RESP_CONFIG_MODE, MyDataConstant.RESP_CONFIG_MODE_REUSE);
        // 若是复用APP，则改用APP的响应配置
        if (MyDataConstant.RESP_CONFIG_MODE_REUSE.equals(mode)) {
            respConfigMap = app.getRespConfig();
        }

        // 响应配置Map转为Bean实例
        PipelineApiResponseConfig responseConfig = BeanUtil.fillBeanWithMap(respConfigMap, PipelineApiResponseConfig.defaultConfig(), true);

        // 校验响应码
        if (responseConfig.isValidCode() && apiResponse.getStatus() != responseConfig.getCodeValue()) {
            throw ExceptionUtil.wrapRuntime("API响应校验不通过，原因：响应码不符合配置，实际{} != 配置{}", apiResponse.getStatus(), responseConfig.getCodeValue());
        }
        // 校验相应内容
        if (responseConfig.isValidBody()) {
            // 实际响应内容
            String responseBody = apiResponse.getBody();
            if (StringUtil.isEmpty(responseBody)) {
                throw ExceptionUtil.wrapRuntime("API响应校验不通过，原因：实际响应内容为空。");
            }

            // 响应体转json对象
            JSON json = JsonUtil.parse(responseBody);
            // 配置的json path
            String jsonPath = responseConfig.getBodyJsonPath();
            // 从json从提取 待验证内容
            String responseValue = json.getByPath(jsonPath, String.class);
            // 配置的内容
            String configBodyValue = responseConfig.getBodyValue();

            // 对比内容是否一致
            if (!StringUtil.equals(responseValue, configBodyValue)) {
                throw ExceptionUtil.wrapRuntime("API响应校验不通过，原因：响应码内容不符合配置，配置规则 path({})={}，实际结果path({})={}", jsonPath, configBodyValue, jsonPath, responseValue);
            }
        }

        return apiResponse;
    }

    /**
     * 流水线任务 调用API
     *
     * @param method      http method
     * @param url         http url
     * @param reqHeaders  request headers
     * @param queryParams request params
     * @param reqForm     request form
     * @param reqBody     request body
     * @param bizData     业务数据，用于替换API定义中的${var}变量
     * @return 流水线API响应
     */
    private PipelineApiResponse callApi(String method, String url, Map<String, String> reqHeaders, Map<String, String> queryParams, Map<String, String> reqForm, String reqBody, Map<String, Object> bizData) {
        // 解析替换系统变量值
        url = JobVarService.processSysVarValue(url);
        JobVarService.processSysVarValues(reqHeaders);
        JobVarService.processSysVarValues(queryParams);
        JobVarService.processSysVarValues(reqForm);
        reqBody = JobVarService.processSysVarValue(reqBody);

        // 替换业务数据变量值
        try {
            JobVarService.processDataFieldVar(queryParams, bizData);
            JobVarService.processDataFieldVar(queryParams, jobContextData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Param中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqHeaders, bizData);
            JobVarService.processDataFieldVar(reqHeaders, jobContextData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Header中的参数出错，原因：" + e.getMessage());
        }
        try {
            JobVarService.processDataFieldVar(reqForm, bizData);
            JobVarService.processDataFieldVar(reqForm, jobContextData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("解析请求Form中的参数出错，原因：" + e.getMessage());
        }
        try {
            reqBody = JobVarService.processDataFieldVar(reqBody, bizData);
            reqBody = JobVarService.processDataFieldVar(reqBody, jobContextData);
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
        try (HttpResponse response = HttpUtil.send(method, url, queryParams, reqHeaders, reqForm, reqBody)) {
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
            Map<String, Object> jwtConfig = (Map<String, Object>) authConfig.get(APP_AUTH_TYPE_JWT);
            AssertUtil.notNull(jwtConfig, "JWT配置无效，请确认");

            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) jwtConfig.get(JWT_CONFIG_API));
            // 认证接口
            AppApi api = MyDataCache.getApi(apiId);

            // 调用认证接口
            PipelineApiResponse apiResponse = callApi(pipelineApp, api, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());

            String responseData = apiResponse.getBody();
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
            String addTo = (String) jwtConfig.get(JWT_CONFIG_ADD_TO);
            // header
            if (MyDataConstant.HTTP_HEADER.equals(addTo)) {
                log("token 添加到 header");
                String key = (String) jwtConfig.get(JWT_CONFIG_HEADER_KEY);
                String prefix = (String) jwtConfig.get(JWT_CONFIG_HEADER_PREFIX);
                prefix = StringUtil.isNotEmpty(prefix) ? prefix : "";
                String value = prefix + token;
                appHeaders.put(key, value);
            } else if (MyDataConstant.HTTP_QUERY.equals(addTo)) {
                log("token 添加到 query param");
                String paramName = (String) jwtConfig.get(JWT_CONFIG_QUERY_PARAM);
                appQueryParams.put(paramName, token);
            }
        }
        // cookie 认证
        else if (APP_AUTH_TYPE_COOKIE.equals(authType)) {
            Map<String, Object> cookieConfig = (Map<String, Object>) authConfig.get(APP_AUTH_TYPE_COOKIE);

            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) cookieConfig.get(COOKIE_CONFIG_API));
            // 认证接口
            AppApi api = MyDataCache.getApi(apiId);

            PipelineApiResponse apiResponse = callApi(pipelineApp, api, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());

            String cookie = apiResponse.getCookie();
            if (StringUtil.isNotEmpty(cookie)) {
                appHeaders.put("Cookie", cookie);
            }
        }
        // api key 认证
        else if (APP_AUTH_TYPE_API_KEY.equals(authType)) {
            Map<String, Object> apiKeyConfig = (Map<String, Object>) authConfig.get(APP_AUTH_TYPE_API_KEY);
            AssertUtil.notNull(apiKeyConfig, "API Key配置无效，请确认");
            // key
            String key = (String) apiKeyConfig.get(ApiTaskExecutor.API_KEY_CONFIG_KEY);
            // value
            String value = (String) apiKeyConfig.get(ApiTaskExecutor.API_KEY_CONFIG_VALUE);
            // add to
            String addTo = (String) apiKeyConfig.get(ApiTaskExecutor.API_KEY_CONFIG_ADD_TO);

            if (MyDataConstant.HTTP_HEADER.equals(addTo)) {
                appHeaders.put(key, value);
            } else {
                appQueryParams.put(key, value);
            }
        }

        authedApps.put(app.getId(), pipelineApp);

        log("应用{} 认证结束。", app.getAppName());
        return pipelineApp;
    }
}
