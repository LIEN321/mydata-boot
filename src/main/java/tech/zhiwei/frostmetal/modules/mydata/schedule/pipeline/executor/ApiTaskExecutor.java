package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

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
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.bean.BeanUtil;
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
    public static final String JWT_HEADER_PREFIX = "prefix";
    public static final String JWT_HEADER_AUTHORIZATION = "Authorization";
    public static final String JWT_QUERY_PARAM = "param";

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
     * 执行app认证
     *
     * @param app 待执行的app
     */
    public PipelineApp doAppAuth(App app) {
        AssertUtil.notNull(app, "应用认证失败：待认证的应用无效");

        PipelineApp pipelineApp = BeanUtil.copyProperties(app, PipelineApp.class);
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

        log("应用{} 认证开始...", app.getAppName());
        // jwt 认证
        if (MyDataConstant.APP_AUTH_TYPE_JWT.equals(authType)) {
            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) authConfig.get(AUTH_CONFIG_API));
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
            PipelineApiResponse apiResponse = JobApiService.callApi(this, api.getApiMethod(), StringUtil.emptyIfNull(app.getApiPrefix()) + api.getApiUri(), reqHeaders, apiReqParams, reqForm, reqBody, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());

            // 获取接口返回的json
            JSON json = JsonUtil.parse(apiResponse.getData());
            // json中提取token
            String token = (String) json.getByPath(api.getFieldPrefix());

            // add to
            String addTo = (String) authConfig.get(AUTH_CONFIG_ADD_TO);
            // header
            if (MyDataConstant.HTTP_HEADER.equals(addTo)) {
                String prefix = (String) authConfig.get(JWT_HEADER_PREFIX);
                prefix = StringUtil.isNotEmpty(prefix) ? prefix + " " : "";
                String value = prefix + token;
                appHeaders.put(JWT_HEADER_AUTHORIZATION, value);
            } else if (MyDataConstant.HTTP_QUERY.equals(addTo)) {
                String paramName = (String) authConfig.get(JWT_QUERY_PARAM);
                appQueryParams.put(paramName, token);
            }
        }
        // cookie 认证
        else if (MyDataConstant.APP_AUTH_TYPE_COOKIE.equals(authType)) {
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
        else if (MyDataConstant.APP_AUTH_TYPE_API_KEY.equals(authType)) {
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
