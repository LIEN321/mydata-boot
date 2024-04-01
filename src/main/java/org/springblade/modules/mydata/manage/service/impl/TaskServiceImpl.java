package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MapUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.job.executor.JobExecutor;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.TaskDTO;
import org.springblade.modules.mydata.manage.dto.TaskStatDTO;
import org.springblade.modules.mydata.manage.entity.Api;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.DataField;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.entity.Project;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.mail.MailSender;
import org.springblade.modules.mydata.manage.mapper.TaskMapper;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.TaskVO;
import org.springblade.modules.mydata.manage.wrapper.TaskWrapper;
import org.springblade.modules.system.entity.UserInfo;
import org.springblade.modules.system.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 集成任务 服务实现类
 *
 * @author LIEN
 * @since 2022-07-11
 */
@Service
@AllArgsConstructor
@Slf4j
public class TaskServiceImpl extends BaseServiceImpl<TaskMapper, Task> implements ITaskService {

    @Resource
    private final IDataFieldService dataFieldService;

    @Resource
    private final ITaskLogService taskLogService;

    @Resource
    private final IUserService userService;

    @Resource
    private final JobExecutor jobExecutor;

    @Override
    public IPage<TaskVO> selectTaskPage(IPage<TaskVO> page, TaskVO task) {
        return page.setRecords(baseMapper.selectTaskPage(page, task));
    }

    @Override
    public IPage<TaskVO> taskPage(IPage<Task> page, Wrapper<Task> queryWrapper) {
        IPage<Task> pages = this.page(page, queryWrapper);
        List<Task> tasks = pages.getRecords();

        IPage<TaskVO> taskPage = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
        List<TaskVO> taskVOList = TaskWrapper.build().listVO(tasks);
        taskPage.setRecords(taskVOList);
        return taskPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean submit(TaskDTO taskDTO) {
        // 校验参数
        check(taskDTO);

        // v0.6.0取消状态验证，调整为：运行状态可以提交修改，但需要手动重启任务
        // 查询任务状态，若是运行状态 则不能提交
//        Task check = getById(taskDTO.getId());
//        Assert.isFalse(check != null && MdConstant.TASK_STATUS_RUNNING == check.getTaskStatus(), "提交失败：任务处于运行状态，不可编辑！");

        Long dataId = taskDTO.getDataId();
        // 查询data
        Data data = ManageCache.getData(dataId);
//        Assert.notNull(data, "提交失败：所选数据项 不存在！");

        // 查询data的主键字段
        List<DataField> idFields = null;
        if (data != null) {
            idFields = dataFieldService.findIdFields(taskDTO.getDataId());
        }
//        Assert.notEmpty(idFields, "提交失败：所选数据项 缺少唯一标识字段！");

        // 查询project
        Project project = ManageCache.getProject(taskDTO.getProjectId());
        Assert.notNull(project, "提交失败：项目 不存在！");

        // 查询api
        Api api = ManageCache.getApi(taskDTO.getApiId());
        Assert.notNull(api, "提交失败：所选API 不存在！");

        // 查询环境
        Env env = ManageCache.getEnv(taskDTO.getEnvId());
        Assert.notNull(env, "提交失败：环境 不存在！");

        // 查询跨环境任务的对应目标环境
        Env refEnv = null;
        if (taskDTO.getRefEnvId() != null) {
            refEnv = ManageCache.getEnv(taskDTO.getRefEnvId());
            Assert.notNull(refEnv, "提交失败：外部环境 不存在！");
        }

        Task task = BeanUtil.copyProperties(taskDTO, Task.class);
        // 复制data的编号
        if (data != null) {
            task.setDataCode(data.getDataCode());
        }
        // 复制标识字段的编号
        if (idFields != null) {
            List<String> idFieldCodes = idFields.stream().map(DataField::getFieldCode).collect(Collectors.toList());
            task.setIdFieldCode(CollUtil.join(idFieldCodes, StrPool.COMMA));
        }
        // 复制api的操作类型
        task.setOpType(api.getOpType());
        // 复制api的请求方法
        task.setApiMethod(api.getApiMethod());
        // 复制api的数据类型
        task.setDataType(api.getDataType());
        // 复制api的所属应用
        task.setAppId(api.getAppId());

        // 从env和api中 汇总header、param，优先级api > env
        if (refEnv != null) {
            mergeApiAndEnv(task, api, refEnv);
            task.setRefOpType(task.getOpType() == MdConstant.DATA_PRODUCER ? MdConstant.DATA_CONSUMER : MdConstant.DATA_PRODUCER);
        } else {
            mergeApiAndEnv(task, api, env);
        }


        if (task.getId() == null) {
            task.setTaskStatus(MdConstant.TASK_STATUS_STOPPED);
        }

        // 保存或更新task
        return saveOrUpdate(task);

        // v0.6.0 取消自动重启，改为用户先停止 再修改 启动；
//        boolean result = saveOrUpdate(task);
//        if (result) {
//            // 若任务已启动，则自动重启
//            task = getById(task.getId());
//            if (task != null && task.getTaskStatus() != null && task.getTaskStatus() == MdConstant.TASK_STATUS_RUNNING) {
//                restartTask(task.getId());
//            }
//        }
//        return result;
    }

    @Override
    public TaskVO detail(Long id) {
        Assert.notNull(id);

        Task task = getById(id);

        Assert.notNull(task);

        return TaskWrapper.build().entityVO(task);
    }

    //    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean startTask(Long id) {
        // 校验参数
        Assert.notNull(id);

        // 更新任务状态为启动
        Task task = getById(id);
        Assert.notNull(task, "启动失败，任务无效！");
        task.setTaskStatus(MdConstant.TASK_STATUS_RUNNING);
        boolean result = updateById(task);

        // 非订阅模式的任务 才能启动（fix #707419847）
        if (result && !MdConstant.TASK_IS_SUBSCRIBED.equals(task.getIsSubscribed())) {
            // 通知任务服务
            try {
                jobExecutor.startTask(id);
            } catch (Exception e) {
                // TODO 优化对job服务访问异常的处理
                e.printStackTrace();
                throw new ServiceException("任务启动失败，请联系管理员！");
            }
        }
        return result;
    }

    //    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean stopTask(Long id) {
        // 校验参数
        Assert.notNull(id);

        // 更新任务状态为停止
        Task task = getById(id);
        Assert.notNull(task, "停止失败，任务无效！");
        task.setTaskStatus(MdConstant.TASK_STATUS_STOPPED);

        boolean result = updateById(task);

        if (result) {
            // 通知任务服务
            try {
                jobExecutor.stopTask(id);
            } catch (Exception e) {
                // TODO 优化对job服务访问异常的处理
                e.printStackTrace();
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean restartTask(Long id) {
        return stopTask(id) && startTask(id);
    }

    @Override
    public boolean executeTask(Long id) {
        try {
            jobExecutor.executeOnce(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> listRunningTasks() {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING).ne(Task::getIsSubscribed, MdConstant.TASK_IS_SUBSCRIBED);
        return list(queryWrapper);
    }

    @Override
    public List<Task> listEnvTaskByData(Long dataId, Long envId) {
//        Assert.notNull(dataId, "参数dataId无效，dataId = {}", dataId);
//        Assert.notNull(envId, "参数envId无效，envId = {}", envId);
        if (ObjectUtil.hasNull(dataId, envId)) {
            return null;
        }

        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getDataId, dataId).and(qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));

        return list(queryWrapper);
    }

    @Override
    public List<Task> listRunningSubTasks(Long dataId) {
        Assert.notNull(dataId, "参数dataId无效，dataId = {}", dataId);

        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING).eq(Task::getIsSubscribed, MdConstant.TASK_IS_SUBSCRIBED).eq(Task::getDataId, dataId);

        return list(queryWrapper);
    }

    @Override
    public List<Task> listSuccessTasks() {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING).orderByDesc(Task::getLastSuccessTime);

        IPage<Task> page = new Page<>();
        page.setSize(MdConstant.PAGE_SIZE);
        IPage<Task> taskPage = page(page, queryWrapper);
        return taskPage.getRecords();
    }

    @Override
    public List<Task> listFailedTasks() {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getTaskStatus, MdConstant.TASK_STATUS_FAILED).orderByDesc(Task::getLastSuccessTime);

        IPage<Task> page = new Page<>();
        page.setSize(MdConstant.PAGE_SIZE);
        IPage<Task> taskPage = page(page, queryWrapper);
        return taskPage.getRecords();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean failTask(Long id) {
        Assert.notNull(id);

        // 更新任务为失败状态
        Task task = getById(id);
        task.setTaskStatus(MdConstant.TASK_STATUS_FAILED);
        boolean result = updateById(task);

        // 发送失败邮件给任务创建人
        try {
            if (result) {
                // 查询创建人的邮件
                UserInfo userInfo = userService.userInfo(task.getCreateUser());
                String emailAddress = userInfo.getUser().getEmail();
                if (StrUtil.isNotBlank(emailAddress)) {
                    String messageId = MailSender.sendMail(emailAddress, StrUtil.format("mydata通知 定时任务【{}】异常停止", task.getTaskName()), StrUtil.format("定时任务【{}】异常停止，时间：{}，异常信息请详见任务日志。", task.getTaskName(), DateUtil.now()));
                    log.info("email messageId = {}", messageId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(Long id) {
        Task task = ManageCache.getTask(id);
        if (task == null) {
            return true;
        }
        if (task.getTaskStatus() == MdConstant.TASK_STATUS_RUNNING) {
            throw new ServiceException("删除失败，任务正在运行！");
        }

        taskLogService.deleteByTask(id);
        return removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(List<Long> ids) {
        ids.forEach(this::delete);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByApi(Long apiId) {
        List<Task> tasks = list(null, apiId, null);
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(task -> this.delete(task.getId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByEnv(Long envId) {
        List<Task> tasks = list(null, null, envId);
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(task -> this.delete(task.getId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteByData(Long dataId) {
        List<Task> tasks = list(dataId, null, null);
        if (CollUtil.isNotEmpty(tasks)) {
            tasks.forEach(task -> this.delete(task.getId()));
        }
        return true;
    }

    //    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateApiUrlByEnv(Env env) {
        // 根据环境查询任务
        List<Task> tasks = list(null, null, env.getId());
        if (CollUtil.isNotEmpty(tasks)) {
            // 批量更新任务的接口地址和参数
            tasks.forEach(task -> {
                Api api = ManageCache.getApi(task.getApiId());
                if (api != null) {
                    mergeApiAndEnv(task, api, env);
                }
            });
            updateBatchById(tasks);

            // 筛选运行中的任务
            List<Task> runningTasks = tasks.stream().filter(task -> task.getTaskStatus() == MdConstant.TASK_STATUS_RUNNING).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(runningTasks)) {
                // 重启任务的调度
                try {
                    runningTasks.forEach(task -> jobExecutor.restartTask(task.getId()));
                } catch (Exception e) {
                    // TODO 优化对job服务访问异常的处理
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    //    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateApiUrlByApi(Api api) {
        // 根据环境查询任务
        List<Task> tasks = list(null, api.getId(), null);
        if (CollUtil.isNotEmpty(tasks)) {
            // 批量更新任务的api地址
            tasks.forEach(task -> {
                Env env = ManageCache.getEnv(task.getEnvId());
                if (env != null) {
                    mergeApiAndEnv(task, api, env);
                }
            });
            updateBatchById(tasks);

            // 筛选运行中的任务
            List<Task> runningTasks = tasks.stream().filter(task -> task.getTaskStatus() == MdConstant.TASK_STATUS_RUNNING).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(runningTasks)) {
                // 重启任务的调度
                try {
                    runningTasks.forEach(task -> jobExecutor.restartTask(task.getId()));
                } catch (Exception e) {
                    // TODO 优化对job服务访问异常的处理
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public TaskStatDTO getTaskStat() {
        return baseMapper.selectTaskStat();
    }

    @Override
    public boolean finishTask(Task task, List<Map> filteredDataList) {
        Task updateTask = getById(task.getId());
        updateTask.setLastRunTime(task.getLastRunTime());
        updateTask.setLastSuccessTime(task.getLastSuccessTime());
        boolean result = updateById(updateTask);
        if (!result) {
            return result;
        }

        // 若没有被过滤的无效数据，则结束
        if (CollUtil.isEmpty(filteredDataList)) {
            return result;
        }

        // 查询数据项字段
        List<DataField> dataFields = dataFieldService.findByData(updateTask.getDataId());
        if (CollUtil.isEmpty(dataFields)) {
            return result;
        }
        // 字段+数据 构建excel
        File excelFile = FileUtil.createTempFile(MdConstant.TEMP_DIR, ".xls", true);
        ExcelWriter excelWriter = ExcelUtil.getWriter();
        dataFields.forEach(dataField -> {
            excelWriter.addHeaderAlias(dataField.getFieldCode(), dataField.getFieldName());
        });
        excelWriter.write(filteredDataList);
        excelWriter.flush(excelFile);
        excelWriter.close();

        // 发送邮件给任务创建人
        // 查询创建人的邮件
        UserInfo userInfo = userService.userInfo(updateTask.getCreateUser());
        String emailAddress = userInfo.getUser().getEmail();
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress, StrUtil.format("mydata通知：任务【{}】 存在被过滤的无效数据", updateTask.getTaskName()), StrUtil.format("时间：{}，任务【{}】因部分数据不符合过滤条件被拦截，请查看附件。", DateUtil.now(), updateTask.getTaskName()), excelFile);
            log.info("email messageId = {}", messageId);
        }

        return result;
    }

    @Override
    public long countByProjectEnv(Long projectId, Long envId) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getProjectId, projectId).isNotNull(Task::getDataId).and(qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));
        return count(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean copyTask(Long taskId, Long targetEnvId) {
        Task task = ManageCache.getTask(taskId);
        Assert.notNull(task, "复制失败：待复制的任务无效！", taskId);

        Env targetEnv = ManageCache.getEnv(targetEnvId);
        Assert.notNull(targetEnv, "复制失败：目标环境无效！", targetEnvId);

        // 校验task和targetEnv是否同属一个项目
        Assert.equals(task.getProjectId(), targetEnv.getProjectId(), "复制失败：复制的目标环境 与当前任务不属于同一个项目！");

        TaskDTO targetTask = BeanUtil.copyProperties(task, TaskDTO.class, "id", "envId", "taskStatus");
        targetTask.setId(null);
        targetTask.setEnvId(targetEnvId);
        targetTask.setTaskName(targetTask.getTaskName() + " (copy)");

        return submit(targetTask);
    }

    private void check(TaskDTO taskDTO) {
        // 校验参数
        Assert.notNull(taskDTO);

        // 任务名称 不能为空
        String taskName = taskDTO.getTaskName();
        Assert.notBlank(taskName, "名称 不能为空！");
        // 任务名称 去除前后导空格
        taskName = StrUtil.trim(taskName);
        // 任务名称 长度不能超过限制
        Assert.isTrue(StrUtil.length(taskName) <= MdConstant.MAX_NAME_LENGTH, "名称 长度不能超过{}！", MdConstant.MAX_NAME_LENGTH);
        taskDTO.setTaskName(taskName);

        // 所属项目 不能为空
        Assert.notNull(taskDTO.getProjectId(), "提交失败：所属项目无效！");
        // 关联环境 不能为空
        Assert.notNull(taskDTO.getEnvId(), "提交失败：环境无效！");
        // 关联API 不能为空
        Assert.notNull(taskDTO.getApiId(), "提交失败：API无效！");
        // 关联数据项 不能为空 v0.6.0 去掉验证
//        Assert.notNull(taskDTO.getDataId(), "提交失败：数据项无效！");
        // 不是订阅任务，则任务周期必填
        if (!MdConstant.TASK_IS_SUBSCRIBED.equals(taskDTO.getIsSubscribed())) {
            Assert.notBlank(taskDTO.getTaskPeriod(), "提交失败：任务周期 不能为空！");
        }

        // 字段映射 不能为空
        if (taskDTO.getDataId() != null) {
            Map<String, String> fieldMapping = taskDTO.getFieldMapping();
            Assert.notEmpty(fieldMapping, "提交失败：字段映射无效！");

            // 至少有一个字段配置了映射
            Collection<String> values = fieldMapping.values();
            boolean hasValidValue = false;
            for (String value : values) {
                if (StrUtil.isNotBlank(value)) {
                    hasValidValue = true;
                    break;
                }
            }
            Assert.isTrue(hasValidValue, "提交失败：字段映射无效！");
        }
    }

    private List<Task> list(Long dataId, Long apiId, Long envId) {
        LambdaQueryWrapper<Task> queryTaskWrapper = Wrappers.<Task>lambdaQuery().eq(ObjectUtil.isNotNull(dataId), Task::getDataId, dataId).eq(ObjectUtil.isNotNull(apiId), Task::getApiId, apiId).and(ObjectUtil.isNotNull(envId), qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));

        return list(queryTaskWrapper);
    }

    private void mergeApiAndEnv(Task task, Api api, Env env) {
        // 拼接完整的url
        String apiUrl = env.getEnvPrefix() + api.getApiUri();
        task.setApiUrl(apiUrl);
        // 复制api的字段层级前缀
        task.setApiFieldPrefix(api.getFieldPrefix());

        // 从env和api中 汇总header、param，优先级api > env
        LinkedHashMap<String, String> headers = (LinkedHashMap<String, String>) MapUtil.union(env.getGlobalHeaders(), api.getReqHeaders());
        LinkedHashMap<String, String> params = (LinkedHashMap<String, String>) MapUtil.union(env.getGlobalParams(), api.getReqParams());
        task.setReqHeaders(headers);
        task.setReqParams(params);
    }
}
