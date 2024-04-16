package org.springblade.modules.mydata.manage.endpoint;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.tool.api.R;
import org.springblade.modules.mydata.job.executor.JobExecutor;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 数据集成 Endpoint
 *
 * @author LIEN
 * @since 2024/4/12
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(MdConstant.API_PREFIX_MANAGE + "/integration")
public class IntegrationEndpoint {
    @Resource
    private final ITaskService taskService;

    @Resource
    private final JobExecutor jobExecutor;

    @PostMapping("/{task_url}")
    public R post(@PathVariable("task_url") String taskUrl, @RequestHeader HttpHeaders httpHeaders, @RequestBody String body) {
        log.info("integration post");
        return execute(taskUrl, httpHeaders, body);
    }

    @PutMapping("/{task_url}")
    public R put(@PathVariable("task_url") String taskUrl, @RequestHeader HttpHeaders httpHeaders, @RequestBody String body) {
        log.info("integration put");
        return execute(taskUrl, httpHeaders, body);
    }

    private R execute(String taskUrl, HttpHeaders httpHeaders, String body) {
        log.info("integration url : {}", taskUrl);
        log.info("integration headers : {}", httpHeaders);
        log.info("integration body : {}", body);

        Assert.notEmpty(taskUrl, "操作失败：地址 {} 无效！", taskUrl);
        Task task = taskService.findByApiUrl(taskUrl);
        Assert.notNull(task, "操作失败：地址 {} 无效！", taskUrl);
        Assert.equals(task.getTaskStatus(), MdConstant.TASK_STATUS_RUNNING, "操作失败：任务未启动 无法执行！");

        Integer authType = task.getAuthType();
        if (MdConstant.TASK_AUTH_TYPE_NONE.equals(authType)) {
            // do nothing
        } else {
            // 检查认证参数配置是否有效
            Map<String, String> authParams = task.getAuthParams();
            if (CollUtil.isNotEmpty(authParams)) {
                Assert.notNull(task, "操作失败：认证参数未配置！");
            }

            // 校验认证参数
            if (MdConstant.TASK_AUTH_TYPE_API_KEY.equals(authType)) {
                checkApiKey(authParams, httpHeaders);
            } else if (MdConstant.TASK_AUTH_TYPE_BASIC.equals(authType)) {
                checkBasicAuth(authParams, httpHeaders);
            } else if (MdConstant.TASK_AUTH_TYPE_HMAC.equals(authType)) {
                // TODO
            } else {
                throw new IllegalArgumentException("操作失败：不支持任务的认证类型!");
            }
        }

        // 执行任务流程 接收数据
        jobExecutor.acceptData(task, body);
        return R.success(StrUtil.format("任务 [{}] 开始执行，请查看日志。", task.getTaskName()));
    }

    /**
     * 校验 API Key认证
     *
     * @param authParams  任务配置的认证参数
     * @param httpHeaders 请求header
     */
    private void checkApiKey(Map<String, String> authParams, HttpHeaders httpHeaders) {
        // 获取配置的key
        String authKey = authParams.get(MdConstant.TASK_AUTH_API_KEY_HEADER);
        // 判断key是否有效
        Assert.notEmpty(authKey, "操作失败：认证参数key 未正确配置！");

        // 获取配置的value
        String authValue = authParams.get(MdConstant.TASK_AUTH_API_KEY_VALUE);
        // 判断value是否有效
        Assert.notEmpty(authValue, "操作失败：认证参数value 未正确配置！");

        // 从header获取待验证值
        String headerValue = httpHeaders.getFirst(authKey);
        // 判断header值是否有效
        Assert.notEmpty(headerValue, "操作失败：认证参数无效！");
        // 对比value是否一致
        Assert.equals(authValue, headerValue, "操作失败：认证失败！");
    }

    /**
     * 校验 Basic Auth认证
     *
     * @param authParams  任务配置的认证参数
     * @param httpHeaders 请求header
     */
    private void checkBasicAuth(Map<String, String> authParams, HttpHeaders httpHeaders) {
        // 获取配置的username
        String username = authParams.get(MdConstant.TASK_AUTH_BASIC_USERNAME);
        // 判断username是否有效
        Assert.notEmpty(username, "操作失败：认证参数username 未正确配置！");

        // 获取配置的password
        String password = authParams.get(MdConstant.TASK_AUTH_BASIC_PASSWORD);
        // 判断password是否有效
        Assert.notEmpty(password, "操作失败：认证参数password 未正确配置！");

        // 生成配置的auth
        String auth = "Basic " + Base64.encode(username + ":" + password);

        // 从header获取auth
        String headerAuth = httpHeaders.getFirst(MdConstant.TASK_AUTH_BASIC_HEADER);
        // 判断header值是否有效
        Assert.notEmpty(headerAuth, "操作失败：认证参数无效！");

        // 对比value是否一致
        Assert.equals(auth, headerAuth, "操作失败：认证失败！");
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
