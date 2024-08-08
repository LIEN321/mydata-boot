package org.springblade.modules.mydata.manage.wrapper;

import cn.hutool.core.bean.BeanUtil;
import org.springblade.common.constant.MdConstant;
import org.springblade.common.util.MdUtil;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.Task;
import org.springblade.modules.mydata.manage.service.ITaskService;
import org.springblade.modules.mydata.manage.vo.ProjectDataVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目数据项包装类
 *
 * @author LIEN
 * @since 2023/12/12
 */
public class ProjectDataWrapper extends BaseEntityWrapper<Data, ProjectDataVO> {
    private static ITaskService taskService;

    private static BizDataDAO bizDataDAO;

    private Long envId;

    static {
        taskService = SpringUtil.getBean(ITaskService.class);
        bizDataDAO = SpringUtil.getBean(BizDataDAO.class);
    }

    private ProjectDataWrapper(Long envId) {
        this.envId = envId;
    }

    public static ProjectDataWrapper build(Long envId) {
        return new ProjectDataWrapper(envId);
    }

    @Override
    public ProjectDataVO entityVO(Data data) {
        ProjectDataVO projectDataVO = BeanUtil.copyProperties(data, ProjectDataVO.class, "dataCount");

        List<Task> tasks = taskService.listEnvTaskByData(data.getId(), envId);
        if (envId != null) {
            long totalCount = bizDataDAO.total(MdUtil.getBizDbCode(data.getTenantId(), data.getProjectId(), envId), data.getDataCode(), null);
            projectDataVO.setDataCount(totalCount);

            long provideAppCount = tasks.stream()
                    .filter(t -> (t.getEnvId().equals(envId) && t.getOpType() == MdConstant.DATA_PRODUCER)
                            || (t.getRefEnvId() != null && t.getRefEnvId()
                            .equals(envId) && t.getRefOpType() == MdConstant.DATA_PRODUCER))
                    .map(Task::getAppId)
                    .collect(Collectors.toSet())
                    .size();
            long consumeAppCount = tasks.stream()
                    .filter(t -> (t.getEnvId().equals(envId) && t.getOpType() == MdConstant.DATA_CONSUMER)
                            || (t.getRefEnvId() != null && t.getRefEnvId()
                            .equals(envId) && t.getRefOpType() == MdConstant.DATA_CONSUMER))
                    .map(Task::getAppId)
                    .collect(Collectors.toSet())
                    .size();

            long runningTaskCount = tasks.stream()
                    .filter(t -> t.getTaskStatus() == MdConstant.TASK_STATUS_STARTED)
                    .count();

            long stoppedTaskCount = tasks.stream()
                    .filter(t -> t.getTaskStatus() == MdConstant.TASK_STATUS_STOPPED)
                    .count();

            long failedTaskCount = tasks.stream()
                    .filter(t -> t.getTaskStatus() == MdConstant.TASK_STATUS_FAILED)
                    .count();

            projectDataVO.setProvideAppCount(provideAppCount);
            projectDataVO.setConsumeAppCount(consumeAppCount);
            projectDataVO.setRunningTaskCount(runningTaskCount);
            projectDataVO.setStoppedTaskCount(stoppedTaskCount);
            projectDataVO.setFailedTaskCount(failedTaskCount);
        }

        return projectDataVO;
    }
}
