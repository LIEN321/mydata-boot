package tech.zhiwei.frostmetal.modules.mydata.manage.webhook;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zhiwei.frostmetal.core.base.common.R;
import tech.zhiwei.frostmetal.modules.mydata.constant.MyDataConstant;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.Pipeline;
import tech.zhiwei.frostmetal.modules.mydata.manage.service.IPipelineService;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.PipelineScheduler;
import tech.zhiwei.tool.codec.Base64;
import tech.zhiwei.tool.collection.CollectionUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;

import java.util.Map;

/**
 * 触发流水线的webhook接口
 *
 * @author LIEN
 * @since 2024/11/27
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@AllArgsConstructor
@Tag(name = "pipelineWebhook", description = "触发流水线的webhook接口")
@Hidden
public class PipelineWebhookEndpoint {
    @Resource
    private IPipelineService pipelineService;

    @Resource
    private PipelineScheduler pipelineScheduler;

    @GetMapping("/{pipelineId}/{code}")
    public R get(@PathVariable("pipelineId") Long pipelineId
            , @PathVariable("code") String code
            , @RequestHeader(required = false) HttpHeaders httpHeaders
            , @RequestBody(required = false) String body) {
        log.info("pipeline webhook get");
        return execute(pipelineId, code, httpHeaders, body);
    }

    @PostMapping("/{pipelineId}/{code}")
    public R post(@PathVariable("pipelineId") Long pipelineId
            , @PathVariable("code") String code
            , @RequestHeader(required = false) HttpHeaders httpHeaders
            , @RequestBody(required = false) String body) {
        log.info("pipeline webhook post");
        return execute(pipelineId, code, httpHeaders, body);
    }

    @PutMapping("/{pipelineId}/{code}")
    public R put(@PathVariable("pipelineId") Long pipelineId
            , @PathVariable("code") String code
            , @RequestHeader(required = false) HttpHeaders httpHeaders
            , @RequestBody(required = false) String body) {
        log.info("pipeline webhook put");
        return execute(pipelineId, code, httpHeaders, body);
    }

    private R execute(Long pipelineId, String code, HttpHeaders httpHeaders, String body) {
        log.info("integration pipelineId : {}", pipelineId);
        log.info("integration code : {}", code);
        log.info("integration headers : {}", httpHeaders);
        log.info("integration body : {}", body);

        AssertUtil.notNull(pipelineId, "操作失败：参数pipelineId={} 无效！", pipelineId);
        Pipeline pipeline = pipelineService.getById(pipelineId);
        AssertUtil.notNull(pipeline, "操作失败：参数pipelineId={} 无效！", pipelineId);
        AssertUtil.isTrue(pipeline.getIsWebhook(), "操作失败：流水线未启动webhook 无法执行！");

        Integer authType = pipeline.getWebhookAuthType();
        if (MyDataConstant.TASK_AUTH_TYPE_NONE.equals(authType)) {
            // do nothing
        } else {
            // 检查认证参数配置是否有效
            Map<String, Object> authParams = pipeline.getWebhookAuthParams();
            if (CollectionUtil.isNotEmpty(authParams)) {
                AssertUtil.notNull(pipeline, "操作失败：认证参数未配置！");
            }

            // 校验认证参数
            if (MyDataConstant.TASK_AUTH_TYPE_API_KEY.equals(authType)) {
                checkApiKey(authParams, httpHeaders);
            } else if (MyDataConstant.TASK_AUTH_TYPE_BASIC.equals(authType)) {
                checkBasicAuth(authParams, httpHeaders);
            } else if (MyDataConstant.TASK_AUTH_TYPE_HMAC.equals(authType)) {
                // TODO
            } else {
                throw new IllegalArgumentException("操作失败：不支持任务的认证类型!");
            }
        }

        // 执行任务流程 接收数据
        pipelineScheduler.webhookPipeline(pipelineId);
        return R.success(StringUtil.format("流水线 [{}] 开始执行，请查看日志。", pipeline.getPipelineName()));
    }

    /**
     * 校验 API Key认证
     *
     * @param authParams  任务配置的认证参数
     * @param httpHeaders 请求header
     */
    private void checkApiKey(Map<String, Object> authParams, HttpHeaders httpHeaders) {
        Map<String, String> apiKey = (Map<String, String>) authParams.get(MyDataConstant.TASK_AUTH_API_KEY);
        AssertUtil.notEmpty(apiKey, "操作失败：Api Key认证参数未正确配置！");

        // 获取配置的key
        String authKey = apiKey.get(MyDataConstant.TASK_AUTH_API_KEY_KEY);
        // 判断key是否有效
        AssertUtil.notEmpty(authKey, "操作失败：Api Key认证参数key 未正确配置！");

        // 获取配置的value
        String authValue = apiKey.get(MyDataConstant.TASK_AUTH_API_KEY_VALUE);
        // 判断value是否有效
        AssertUtil.notEmpty(authValue, "操作失败：Api Key认证参数value 未正确配置！");

        // 从header获取待验证值
        String headerValue = httpHeaders.getFirst(authKey);
        // 判断header值是否有效
        AssertUtil.notEmpty(headerValue, "操作失败：Api Key认证参数无效！");
        // 对比value是否一致
        AssertUtil.equals(authValue, headerValue, "操作失败：Api Key认证失败！");
    }

    /**
     * 校验 Basic Auth认证
     *
     * @param authParams  任务配置的认证参数
     * @param httpHeaders 请求header
     */
    private void checkBasicAuth(Map<String, Object> authParams, HttpHeaders httpHeaders) {
        Map<String, String> basicAuth = (Map<String, String>) authParams.get(MyDataConstant.TASK_AUTH_BASIC);
        AssertUtil.notEmpty(basicAuth, "操作失败：Basic Auth认证参数未正确配置！");

        // 获取配置的username
        String username = basicAuth.get(MyDataConstant.TASK_AUTH_BASIC_USERNAME);
        // 判断username是否有效
        AssertUtil.notEmpty(username, "操作失败：Basic Auth认证参数username 未正确配置！");

        // 获取配置的password
        String password = basicAuth.get(MyDataConstant.TASK_AUTH_BASIC_PASSWORD);
        // 判断password是否有效
        AssertUtil.notEmpty(password, "操作失败：Basic Auth认证参数password 未正确配置！");

        // 生成配置的auth
        String auth = "Basic " + Base64.encode(username + ":" + password);

        // 从header获取auth
        String headerAuth = httpHeaders.getFirst(MyDataConstant.TASK_AUTH_BASIC_HEADER);
        // 判断header值是否有效
        AssertUtil.notEmpty(headerAuth, "操作失败：Basic Auth认证参数无效！");

        // 对比value是否一致
        AssertUtil.equals(auth, headerAuth, "操作失败：Basic Auth认证失败！");
    }

    /**
     * 校验 HMAC认证
     *
     * @param authParams  任务配置的认证参数
     * @param httpHeaders 请求header
     */
    private void checkHMAC(Map<String, String> authParams, HttpHeaders httpHeaders, String body) {
        // TODO
    }
}
