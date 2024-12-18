package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.service;

import org.springframework.stereotype.Component;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.App;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.AppApi;
import tech.zhiwei.frostmetal.modules.mydata.util.MyDataUtil;
import tech.zhiwei.tool.http.HttpUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.ObjectUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.util.Map;

/**
 * 任务调用API的服务类
 *
 * @author LIEN
 * @since 2024/12/16
 */
@Component
public class JobApiService {
    private final JobVarService jobVarService = SpringUtil.getBean(JobVarService.class);

    /**
     * 调用应用的接口
     *
     * @param app 应用
     * @param api 接口
     * @return 接口响应内容
     */
    public String callApi(App app, AppApi api, Map<String, String> batchParams, Map<String, String> bodyVarMap) {
        AssertUtil.notNull(app);
        AssertUtil.notNull(api);

        // API的请求参数
        Map<String, String> reqParams = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(api.getReqParams()));
        if (MapUtil.isNotEmpty(batchParams)) {
            reqParams = MapUtil.union(reqParams, batchParams);
        }

        // APP 全局Header
        Map<String, String> appHeaders = ObjectUtil.cloneByStream(MyDataUtil.parseToKvMapObj(app.getReqHeaders()));
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
            if (MapUtil.isNotEmpty(bodyVarMap)) {
                reqBody = StringUtil.substitute(reqBody, bodyVarMap);
            }
        }

        // 应用统一的api地址前缀
        String apiPrefix = app.getApiPrefix();
        // api地址
        String apiUrl = StringUtil.emptyIfNull(apiPrefix) + api.getApiUri();

        // 解析替换系统变量值
        apiUrl = jobVarService.processSysVarValue(apiUrl);
        jobVarService.processSysVarValues(reqParams);
        jobVarService.processSysVarValues(reqHeaders);
        jobVarService.processSysVarValues(reqForm);
        if (reqBody != null) {
            reqBody = jobVarService.processSysVarValue(reqBody);
        }

        // 发送请求
        return HttpUtil.send(api.getApiMethod(), apiUrl, reqParams, reqHeaders, reqForm, reqBody);
    }
}
