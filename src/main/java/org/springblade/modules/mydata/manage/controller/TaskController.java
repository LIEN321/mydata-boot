package org.springblade.modules.mydata.manage.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.MdConstant;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.TaskDTO;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.entity.TaskLog;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.ProjectDataTaskVO;
import org.springblade.modules.mydata.manage.vo.TaskLogVO;
import org.springblade.modules.mydata.manage.vo.TaskVO;
import org.springblade.modules.mydata.manage.wrapper.TaskLogWrapper;
import org.springblade.modules.mydata.manage.wrapper.TaskWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 集成任务 控制器
 *
 * @author LIEN
 * @since 2022-07-11
 */
@RestController
@AllArgsConstructor
@RequestMapping(MdConstant.API_PREFIX_MANAGE + "/task")
@Api(value = "集成任务", tags = "集成任务接口")
public class TaskController extends BladeController {

    private final ITaskService taskService;

    private final ITaskLogService taskLogService;

    /**
     * 详情
     */
    @GetMapping("/detail")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "详情", notes = "传入task")
    public R<TaskVO> detail(@RequestParam Long id) {
        return R.data(taskService.detail(id));
    }

    /**
     * 分页 集成任务
     */
    @GetMapping("/list")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "分页", notes = "传入task")
    public R<IPage<TaskVO>> list(Task task, Query query) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.lambdaQuery();
        if (task != null) {
            queryWrapper.like(ObjectUtil.isNotNull(task.getTaskName()), Task::getTaskName, task.getTaskName());
        }
        return R.data(taskService.taskPage(Condition.getPage(query), queryWrapper));
    }

    @GetMapping("/data_tasks")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "数据项的同步任务列表", notes = "传入task")
    public R<ProjectDataTaskVO> listProjectDataTask(Task task) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Task::getProjectId, task.getProjectId())
                    .eq(Task::getDataId, task.getDataId())
                    .and(qw -> qw.eq(Task::getEnvId, task.getEnvId()).or().eq(Task::getRefEnvId, task.getEnvId()));
        List<Task> tasks = taskService.list(queryWrapper);

        ProjectDataTaskVO projectDataVO = new ProjectDataTaskVO();
        if (CollUtil.isNotEmpty(tasks)) {
            List<Task> producerTasks = tasks.stream()
                                            .filter(t -> (t.getEnvId()
                                                           .equals(task.getEnvId()) && t.getOpType() == MdConstant.DATA_PRODUCER) || (t.getRefEnvId() != null && t.getRefEnvId()
                                                                                                                                                                  .equals(task.getEnvId()) && t.getRefOpType() == MdConstant.DATA_PRODUCER))
                                            .collect(Collectors.toList());
            List<Task> consumerTasks = tasks.stream()
                                            .filter(t -> (t.getEnvId()
                                                           .equals(task.getEnvId()) && t.getOpType() == MdConstant.DATA_CONSUMER) || (t.getRefEnvId() != null && t.getRefEnvId()
                                                                                                                                                                  .equals(task.getEnvId()) && t.getRefOpType() == MdConstant.DATA_CONSUMER))
                                            .collect(Collectors.toList());

            TaskWrapper taskWrapper = TaskWrapper.build();
            projectDataVO.setProducerTasks(taskWrapper.listVO(producerTasks));
            projectDataVO.setConsumerTasks(taskWrapper.listVO(consumerTasks));
        }

        return R.data(projectDataVO);
    }

    @GetMapping("/env_tasks")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "数据项的同步任务列表", notes = "传入task")
    public R<ProjectDataTaskVO> listProjectEnvTasks(Task task) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.isNull(Task::getDataId).eq(Task::getEnvId, task.getEnvId());
        List<Task> tasks = taskService.list(queryWrapper);

        ProjectDataTaskVO projectDataVO = new ProjectDataTaskVO();
        if (CollUtil.isNotEmpty(tasks)) {
            List<Task> producerTasks = tasks.stream()
                                            .filter(t -> t.getOpType() == MdConstant.DATA_PRODUCER)
                                            .collect(Collectors.toList());
            List<Task> consumerTasks = tasks.stream()
                                            .filter(t -> t.getOpType() == MdConstant.DATA_CONSUMER)
                                            .collect(Collectors.toList());

            TaskWrapper taskWrapper = TaskWrapper.build();
            projectDataVO.setProducerTasks(taskWrapper.listVO(producerTasks));
            projectDataVO.setConsumerTasks(taskWrapper.listVO(consumerTasks));
        }

        return R.data(projectDataVO);
    }

    /**
     * 自定义分页 集成任务
     */
    @GetMapping("/page")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "分页", notes = "传入task")
    public R<IPage<TaskVO>> page(TaskVO task, Query query) {
        IPage<TaskVO> pages = taskService.selectTaskPage(Condition.getPage(query), task);
        return R.data(pages);
    }

    /**
     * 新增 集成任务
     */
    @PostMapping("/save")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "新增", notes = "传入task")
    public R save(@Valid @RequestBody Task task) {
        boolean result = taskService.save(task);
        if (result) {
            ManageCache.clearTask(task.getId());
        }
        return R.status(result);
    }

    /**
     * 修改 集成任务
     */
    @PostMapping("/update")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "修改", notes = "传入task")
    public R update(@Valid @RequestBody Task task) {
        boolean result = taskService.updateById(task);
        if (result) {
            ManageCache.clearTask(task.getId());
        }
        return R.status(result);
    }

    /**
     * 新增或修改 集成任务
     */
    @PostMapping("/submit")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "新增或修改", notes = "传入task")
    public R submit(@Valid @RequestBody TaskDTO taskDTO) {
        boolean result = taskService.submit(taskDTO);
        if (result) {
            ManageCache.clearTask(taskDTO.getId());
        }
        return R.status(result);
    }


    /**
     * 删除 集成任务
     */
    @PostMapping("/remove")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "逻辑删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        boolean result = taskService.delete(Func.toLongList(ids));
        if (result) {
            ManageCache.clearTask(Func.toLongArray(ids));
        }
        return R.status(result);
    }


    /**
     * 启动任务
     *
     * @param id 任务id
     */
    @PostMapping("/start/{id}")
    public R startTask(@PathVariable Long id) {
        boolean result = taskService.startTask(id);
        if (result) {
            ManageCache.clearTask(id);
        }
        return R.status(result);
    }

    /**
     * 停止任务
     *
     * @param id 任务id
     */
    @PostMapping("/stop/{id}")
    public R stopTask(@PathVariable Long id) {
        boolean result = taskService.stopTask(id);
        if (result) {
            ManageCache.clearTask(id);
        }
        return R.status(result);
    }

    /**
     * 停止任务
     *
     * @param id 任务id
     */
    @PostMapping("/execute/{id}")
    public R execute(@PathVariable Long id) {
        return R.status(taskService.executeTask(id));
    }

    /**
     * 任务 日志列表
     */
    @GetMapping("/logs")
    public R<IPage<TaskLogVO>> listLogs(TaskLog taskLog, Query query) {
        IPage<TaskLog> tasks = taskLogService.listTaskLogPage(Condition.getPage(query), taskLog);
        return R.data(TaskLogWrapper.build().pageVO(tasks));
    }

    @PostMapping("/copy_task")
    public R copyTask(@RequestParam Long taskId, @RequestParam Long envId) {
        return R.status(taskService.copyTask(taskId, envId));
    }

    /**
     * 日志详情
     */
    @GetMapping("/log_detail")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "详情", notes = "传入task")
    public R<TaskLogVO> logDetail(@RequestParam Long id) {
        return R.data(TaskLogWrapper.build().detailVO(taskLogService.getById(id)));
    }

    @GetMapping("/env_produce_tasks")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "数据项的同步任务列表", notes = "传入task")
    public R<List<TaskVO>> listEnvProduceTask(Task task) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Task::getProjectId, task.getProjectId())
                    .eq(Task::getDataId, task.getDataId())
                    .eq(Task::getEnvId, task.getEnvId())
                    .eq(Task::getOpType, MdConstant.DATA_PRODUCER);
        List<Task> tasks = taskService.list(queryWrapper);
        return R.data(TaskWrapper.build().listVO(tasks));
    }
}
