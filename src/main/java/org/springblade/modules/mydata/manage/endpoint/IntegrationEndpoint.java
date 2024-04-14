package org.springblade.modules.mydata.manage.endpoint;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.tool.api.R;
import org.springblade.modules.mydata.job.executor.JobExecutor;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 数据集成 Endpoint
 *
 * @author LIEN
 * @since 2024/4/12
 */
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
        Assert.notEmpty(taskUrl, "集成失败：地址 {} 无效！", taskUrl);
        Task task = taskService.findByApiUrl(taskUrl);
        Assert.notNull(task, "集成失败：地址 {} 无效！", taskUrl);
        jobExecutor.acceptData(task, body);
        return R.success(StrUtil.format("任务 {} 开始执行，请查看日志。", task.getTaskName()));
    }
}
