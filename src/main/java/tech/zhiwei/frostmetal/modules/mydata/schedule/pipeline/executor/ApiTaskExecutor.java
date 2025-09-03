package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor;

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
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service.JobApiService;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;
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

    private static final String AUTH_CONFIG_KEY_API = "api";

    /**
     * 已完成授权的App
     */
    private Map<Long, App> authedApps;

    public ApiTaskExecutor(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        authedApps = (Map<Long, App>) jobContextData.get(PipelineJob.PIPELINE_PARAM_KEY_AUTHED_APP);
    }

    /**
     * 执行app认证
     *
     * @param app 待执行的app
     */
    public App doAppAuth(App app) {
        AssertUtil.notNull(app, "应用认证失败：待认证的应用无效");

        // 认证类型
        String authType = app.getAuthType();
        if (StringUtil.isEmpty(authType)) {
            log("应用{} 无需认证...", app.getAppName());
            return app;
        }

        // 检查是否已认证过
        if (authedApps.containsKey(app.getId())) {
            log("应用已在流水线中认证，无需重复认证");
            return authedApps.get(app.getId());
        }

        // 认证配置
        Map<String, Object> authConfig = app.getAuthConfig();

        log("应用{} 认证开始...", app.getAppName());
        // cookie认证
        if (MyDataConstant.AUTH_TYPE_COOKIE.equals(authType)) {
            // 认证的接口id
            Long apiId = NumberUtil.parseLong((String) authConfig.get(AUTH_CONFIG_KEY_API));
            // 认证接口
            AppApi api = MyDataCache.getApi(apiId);

            PipelineApiResponse apiResponse = JobApiService.callApi(this, app, api, null, null, null);
            AssertUtil.equals(apiResponse.getStatus(), ResponseCode.SUCCESS.getCode(), "应用{} 认证失败！", app.getAppName());
        }

        authedApps.put(app.getId(), app);

        log("应用{} 认证成功！", app.getAppName());
        return app;
    }
}
