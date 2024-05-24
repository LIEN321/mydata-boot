package org.springblade.modules.mydata.manage.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.manage.entity.Data;
import org.springblade.modules.mydata.manage.entity.Env;
import org.springblade.modules.mydata.manage.service.IBizDataService;
import org.springblade.modules.mydata.manage.service.IDataService;
import org.springblade.modules.mydata.manage.service.IEnvService;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 管理模块的定时任务
 *
 * @author LIEN
 * @since 2024/05/22
 */
@Slf4j
@Component
public class ManageSchedule {

    @Resource
    private ITaskLogService taskLogService;

    @Resource
    private IDataService dataService;

    @Resource
    private IEnvService envService;

    @Resource
    private IBizDataService bizDataService;

    /**
     * 每天凌晨清理 7天前未执行完的日志记录
     */
    @Scheduled(cron = "10 0 0 * * ?")
    public void clearUnfinishedLog() {
        Date limitDate = DateUtil.offsetDay(new Date(), -7);
        Date endTime = DateUtil.endOfDay(limitDate);
        taskLogService.deleteUnfinished(endTime);
    }

    /**
     * 定时刷新业务数据量
     */
    @Scheduled(cron = "0 0 0/4 * * ?")
    public void refreshBizDataCount() {
        List<Data> dataList = dataService.list();
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        dataList.forEach(data -> {
            Long projectId = data.getProjectId();
            List<Env> envList = envService.listByProject(projectId);
            if (CollUtil.isEmpty(envList)) {
                return;
            }

            envList.forEach(env -> {
                bizDataService.updateDataCount(data.getTenantId(), projectId, env.getId(), data.getId());
            });
        });
    }
}
