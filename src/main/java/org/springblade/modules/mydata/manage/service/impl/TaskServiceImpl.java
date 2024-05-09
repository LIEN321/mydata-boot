package org.springblade.modules.mydata.manage.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MapUtil;
import org.springblade.common.util.MdUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.modules.mydata.job.executor.JobExecutor;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.dto.TaskDTO;
import org.springblade.modules.mydata.manage.dto.TaskStatDTO;
import org.springblade.modules.mydata.manage.entity.*;
import org.springblade.modules.mydata.manage.mapper.TaskMapper;
import org.springblade.modules.mydata.manage.service.IDataFieldService;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.TaskVO;
import org.springblade.modules.mydata.manage.wrapper.TaskWrapper;
import org.springblade.modules.system.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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

        // 查询data
        Long dataId = taskDTO.getDataId();
        Data data = ManageCache.getData(dataId);

        // 查询data的主键字段
        List<DataField> idFields = null;
        List<DataField> dataFields = null;
        if (data != null) {
            //            idFields = dataFieldService.findIdFields(taskDTO.getDataId());
            dataFields = dataFieldService.findByData(taskDTO.getDataId());
            idFields = dataFields.stream()
                    .filter(field -> MdConstant.IS_ID_FIELD.equals(field.getIsId()))
                    .collect(Collectors.toList());
        }
        //        Assert.notEmpty(idFields, "提交失败：所选数据项 缺少唯一标识字段！");

        // 查询project
        Project project = ManageCache.getProject(taskDTO.getProjectId());
        Assert.notNull(project, "提交失败：项目 不存在！");

        // 查询api
        Api api = null;
        if (MdConstant.TASK_PRODUCE_MODE_API.equals(taskDTO.getProduceMode()) || MdConstant.TASK_CONSUME_MODE_API.equals(taskDTO.getConsumeMode())) {
            api = ManageCache.getApi(taskDTO.getApiId());
            Assert.notNull(api, "提交失败：所选API 不存在！");
        }

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
            task.setIdFieldCode(convertIdFieldCode(idFieldCodes));

            // 转换过滤条件的值类型
            List<Map<String, Object>> dataFilters = task.getDataFilter();
            if (CollUtil.isNotEmpty(dataFilters)) {
                // 获取任务配置映射的字段类型
                Map<String, String> fieldTypeMap = dataFields.stream()
                        .collect(Collectors.toMap(DataField::getFieldCode, DataField::getFieldType));
                Map<String, String> fieldTypeMapping = MapUtil.newHashMap();
                task.getFieldMapping().forEach((k, v) -> {
                    fieldTypeMapping.put(k, fieldTypeMap.get(k));
                });

                for (Map<String, Object> filter : dataFilters) {
                    // 过滤条件的字段编号
                    String code = filter.get(MdConstant.PARAM_KEY).toString();
                    // 过滤条件的原值
                    Object value = filter.get(MdConstant.PARAM_VALUE);
                    // 条件类型
                    Object type = filter.get(MdConstant.PARAM_TYPE);
                    // 条件操作
                    String op = filter.get(MdConstant.PARAM_OP).toString();
                    // 不是值类型 则不做类型转换
                    if (!MdConstant.TASK_FILTER_TYPE_VALUE.equals(type)) {
                        continue;
                    }
                    if (MdConstant.DATA_NOT_NULL.equals(op) || MdConstant.DATA_NOT_EMPTY.equals(op)) {
                        continue;
                    }
                    // 转换值类型
                    String targetType = fieldTypeMapping.get(code);
                    try {
                        Object convertValue = MdUtil.convertDataType(value, targetType);
                        filter.put(MdConstant.PARAM_VALUE, convertValue);
                    } catch (Exception e) {
                        throw new RuntimeException(StrUtil.format("过滤条件保存失败，条件 {} 的值 {} 转为目标类型 {} 时出错：{}", code, value, targetType, e.getMessage()));
                    }
                }
            }
        }
        if (api != null) {
            // 复制api的操作类型
            task.setOpType(api.getOpType());
            // 复制api的请求方法
            task.setApiMethod(api.getApiMethod());
            // 复制api的数据类型
            task.setDataType(api.getDataType());
            // 复制api的所属应用
            task.setAppId(api.getAppId());
            // 复制api的请求体
            task.setReqBody(api.getReqBody());

            // 从env和api中 汇总header、param，优先级api > env
            if (refEnv != null) {
                mergeApiAndEnv(task, api, refEnv);
                task.setRefOpType(task.getOpType() == MdConstant.DATA_PRODUCER ? MdConstant.DATA_CONSUMER : MdConstant.DATA_PRODUCER);
            } else {
                mergeApiAndEnv(task, api, env);
                task.setRefOpType(null);
            }
        }

        // 新建任务的初始状态为“停止”
        if (task.getId() == null) {
            task.setTaskStatus(MdConstant.TASK_STATUS_STOPPED);

            // 如果是推送数据，则生成随机地址
            if (MdConstant.TASK_PRODUCE_MODE_PUSH.equals(task.getProduceMode())) {
                task.setTaskPeriod(null);
                task.setRefEnvId(null);
                task.setRefOpType(null);
                do {
                    // 生成随机地址
                    String randomUrl = RandomUtil.randomString(16);
                    // 校验随机地址 是否全局唯一，若不是则再次生成
                    Task check = findByApiUrl(randomUrl);
                    if (check != null) {
                        continue;
                    }

                    // 设置随机地址
                    task.setApiUrl(randomUrl);
                    break;
                } while (true);
            }
        }

        // 保存或更新task
        return saveOrUpdate(task);
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

        // 启动任务前，校验数据是否配置标识字段
        // Assert.notEmpty(task.getIdFieldCode(), "启动失败：数据未设置标识字段！");

        task.setTaskStatus(MdConstant.TASK_STATUS_RUNNING);
        boolean result = updateById(task);

        // 非订阅模式、非接收推送的任务 启动定时任务
        if (result && !MdConstant.TASK_IS_SUBSCRIBED.equals(task.getIsSubscribed()) && !MdConstant.TASK_PRODUCE_MODE_PUSH.equals(task.getProduceMode())) {
            // 通知任务服务
            try {
                jobExecutor.startTask(task, "任务管理启动");
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
        task.setNextRunTime(null);

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
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING)
                .eq(Task::getIsSubscribed, MdConstant.TASK_IS_NOT_SUBSCRIBED)
                .ne(Task::getProduceMode, MdConstant.TASK_PRODUCE_MODE_PUSH);
        return list(queryWrapper);
    }

    @Override
    public List<Task> listEnvTaskByData(Long dataId, Long envId) {
        //        Assert.notNull(dataId, "参数dataId无效，dataId = {}", dataId);
        //        Assert.notNull(envId, "参数envId无效，envId = {}", envId);
        if (ObjectUtil.hasNull(dataId, envId)) {
            return null;
        }

        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getDataId, dataId)
                .and(qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));

        return list(queryWrapper);
    }

    @Override
    public List<Task> listRunningSubTasks(Long dataId, Long envId, Long taskId) {
        Assert.notNull(dataId, "参数dataId无效，dataId = {}", dataId);

        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING)
                .eq(Task::getIsSubscribed, MdConstant.TASK_IS_SUBSCRIBED)
                .eq(Task::getDataId, dataId)
                .eq(Task::getEnvId, envId)
                // 订阅当前taskId 或 全部 的任务
                .and(qw -> qw.eq(Task::getSubscribeTaskId, taskId).or().eq(Task::getSubscribeTaskId, 0));

        return list(queryWrapper);
    }

    @Override
    public List<Task> listSuccessTasks() {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getTaskStatus, MdConstant.TASK_STATUS_RUNNING)
                .orderByDesc(Task::getLastSuccessTime);

        IPage<Task> page = new Page<>();
        page.setSize(MdConstant.PAGE_SIZE);
        IPage<Task> taskPage = page(page, queryWrapper);
        return taskPage.getRecords();
    }

    @Override
    public List<Task> listFailedTasks() {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getTaskStatus, MdConstant.TASK_STATUS_FAILED)
                .orderByDesc(Task::getLastSuccessTime);

        IPage<Task> page = new Page<>();
        page.setSize(MdConstant.PAGE_SIZE);
        IPage<Task> taskPage = page(page, queryWrapper);
        return taskPage.getRecords();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delete(Long id) {
        Task task = getById(id);
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

            // 重启运行的任务
            restartRunningTasks(tasks);
        }
        return true;
    }

    //    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateTaskByApi(Api api) {
        // 根据环境查询任务
        List<Task> tasks = list(null, api.getId(), null);
        if (CollUtil.isNotEmpty(tasks)) {
            // 批量更新任务的api地址
            tasks.forEach(task -> {
                Env env = ManageCache.getEnv(task.getRefEnvId());
                if (env == null) {
                    env = ManageCache.getEnv(task.getEnvId());
                }
                if (env != null) {
                    mergeApiAndEnv(task, api, env);
                }
            });
            updateBatchById(tasks);

            // 重启运行的任务
            restartRunningTasks(tasks);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateIdFieldCode(Long dataId, List<String> idFieldCodes) {
        String codes = convertIdFieldCode(idFieldCodes);

        // 根据环境查询任务
        List<Task> tasks = list(dataId, null, null);
        if (CollUtil.isNotEmpty(tasks)) {
            List<Task> updateTasks = CollUtil.newArrayList();
            // 批量更新任务的api地址
            tasks.forEach(task -> {
                // 比对新旧id集合，若有变化才更新
                if (!codes.equals(task.getIdFieldCode())) {
                    task.setIdFieldCode(codes);
                    updateTasks.add(task);
                }
            });

            if (CollUtil.isNotEmpty(updateTasks)) {
                // 更新任务
                updateBatchById(tasks);

                // 重启运行的任务
                restartRunningTasks(tasks);
            }
        }
    }

    @Override
    public TaskStatDTO getTaskStat() {
        return baseMapper.selectTaskStat();
    }

    @Override
    public void finishTask(Task task) {
        Task updateTask = getById(task.getId());
        updateTask.setLastRunTime(task.getLastRunTime());
        updateTask.setLastSuccessTime(task.getLastSuccessTime());
        updateTask.setTaskStatus(task.getTaskStatus());
        updateById(updateTask);
    }

    @Override
    public long countByProjectEnv(Long projectId, Long envId) {
        LambdaQueryWrapper<Task> queryWrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getProjectId, projectId)
                .isNotNull(Task::getDataId)
                .and(qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));
        return count(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean copyTask(Long taskId, Long targetEnvId) {
        Task task = getById(taskId);
        Assert.notNull(task, "复制失败：待复制的任务无效！", taskId);

        Env targetEnv = ManageCache.getEnv(targetEnvId);
        Assert.notNull(targetEnv, "复制失败：目标环境无效！", targetEnvId);

        // 校验task和targetEnv是否同属一个项目
        Assert.equals(task.getProjectId(), targetEnv.getProjectId(), "复制失败：复制的目标环境 与当前任务不属于同一个项目！");

        TaskDTO targetTask = BeanUtil.copyProperties(task, TaskDTO.class, "id", "envId", "taskStatus");
        targetTask.setId(null);
        targetTask.setEnvId(targetEnvId);
        targetTask.setTaskName(targetTask.getTaskName() + " (copy)");
        // 复制时，清空其他环境信息
        targetTask.setRefEnvId(null);

        return submit(targetTask);
    }

    @Override
    public Task findByApiUrl(String apiUrl) {
        LambdaQueryWrapper<Task> queryTaskWrapper = Wrappers.<Task>lambdaQuery().eq(Task::getApiUrl, apiUrl);
        return getOne(queryTaskWrapper);
    }

    @Override
    public void updateNextRunTime(Long taskId, Date nextRunTime) {
        Assert.notNull(taskId);
        Task task = getById(taskId);
        Assert.notNull(task);
        task.setNextRunTime(nextRunTime);
        updateById(task);
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

        // 提供数据模式是调用API 或 消费数据调用API模式
        if (MdConstant.TASK_PRODUCE_MODE_API.equals(taskDTO.getProduceMode()) || MdConstant.TASK_CONSUME_MODE_API.equals(taskDTO.getConsumeMode())) {
            // 关联API 不能为空
            Assert.notNull(taskDTO.getApiId(), "提交失败：API无效！");
        }
        // 关联数据项 不能为空 v0.6.0 去掉验证
        //        Assert.notNull(taskDTO.getDataId(), "提交失败：数据项无效！");
        // 不是订阅、推送数据 的任务，则任务周期必填
        if (!MdConstant.TASK_IS_SUBSCRIBED.equals(taskDTO.getIsSubscribed()) && !MdConstant.TASK_PRODUCE_MODE_PUSH.equals(taskDTO.getProduceMode())) {
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
        LambdaQueryWrapper<Task> queryTaskWrapper = Wrappers.<Task>lambdaQuery()
                .eq(ObjectUtil.isNotNull(dataId), Task::getDataId, dataId)
                .eq(ObjectUtil.isNotNull(apiId), Task::getApiId, apiId)
                .and(ObjectUtil.isNotNull(envId), qw -> qw.eq(Task::getEnvId, envId).or().eq(Task::getRefEnvId, envId));

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
        task.setReqBody(api.getReqBody());
    }

    private void restartRunningTasks(List<Task> tasks) {
        // 筛选运行中的任务
        List<Task> runningTasks = tasks.stream()
                .filter(task -> task.getTaskStatus() == MdConstant.TASK_STATUS_RUNNING)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(runningTasks)) {
            // 重启任务的调度
            try {
                runningTasks.forEach(task -> jobExecutor.restartTask(task.getId(), "修改API 重启任务"));
            } catch (Exception e) {
                // TODO 优化对job服务访问异常的处理
                e.printStackTrace();
            }
        }
    }

    /**
     * 转换任务的标识字段字符串
     *
     * @param idFieldCodes 标识字段id集合
     * @return 标识字段字符串
     */
    private String convertIdFieldCode(List<String> idFieldCodes) {
        if (CollUtil.isEmpty(idFieldCodes)) {
            return "";
        }
        return CollUtil.join(idFieldCodes, StrPool.COMMA);
    }
}
