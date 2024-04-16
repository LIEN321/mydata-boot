package org.springblade.modules.mydata.manage.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springblade.common.util.MdUtil;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.modules.mydata.manage.cache.ManageCache;
import org.springblade.modules.mydata.manage.entity.Api;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.entity.Project;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.vo.TaskVO;

/**
 * 集成任务包装类,返回视图层所需的字段
 *
 * @author LIEN
 * @since 2022-07-11
 */
public class TaskWrapper extends BaseEntityWrapper<Task, TaskVO> {

    public static TaskWrapper build() {
        return new TaskWrapper();
    }

    @Override
    public TaskVO entityVO(Task task) {
        TaskVO taskVO = BeanUtil.copyProperties(task, TaskVO.class, "fieldVarMapping");

        // 查询数据项所属数据集
        Data data = ManageCache.getData(task.getDataId());
        if (ObjectUtil.isNotNull(data)) {
            taskVO.setDataCode(data.getDataCode());
            taskVO.setDataName(data.getDataName());
        }

        // 查询所属环境
        Env env = ManageCache.getEnv(task.getEnvId());
        if (ObjectUtil.isNotNull(env)) {
            taskVO.setEnvName(env.getEnvName());

            // 查询所属项目
            Project project = ManageCache.getProject(env.getProjectId());
            if (project != null) {
                taskVO.setProjectName(project.getProjectName());
            }
        }

        // 查询应用接口
        Api appApi = ManageCache.getApi(task.getApiId());
        if (ObjectUtil.isNotNull(appApi)) {
            taskVO.setApiName(appApi.getApiName());
        }

        taskVO.setFieldVarMapping(MdUtil.switchMapToList(task.getFieldVarMapping()));

        // 查询所属环境
        if (task.getRefEnvId() != null) {
            Env refEnv = ManageCache.getEnv(task.getRefEnvId());
            if (ObjectUtil.isNotNull(refEnv)) {
                taskVO.setRefEnvName(refEnv.getEnvName());
            }
        }

        // 查询订阅的任务
        if (task.getSubscribeTaskId() != null && task.getSubscribeTaskId() != 0L) {
            Task subscribeTask = ManageCache.getTask(task.getSubscribeTaskId());
            if (ObjectUtil.isNotNull(subscribeTask)) {
                taskVO.setSubscribeTaskName(subscribeTask.getTaskName());
            }
        }

        return taskVO;
    }

}
