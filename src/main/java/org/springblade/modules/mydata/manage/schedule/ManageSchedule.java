package org.springblade.modules.mydata.manage.schedule;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.manage.service.ITaskLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

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

    /**
     * 每天凌晨清理 7天前未执行完的日志记录
     */
    @Scheduled(cron = "10 0 0 * * ?")
    public void clearUnfinishedLog() {
        Date limitDate = DateUtil.offsetDay(new Date(), -7);
        Date endTime = DateUtil.endOfDay(limitDate);
        taskLogService.deleteUnfinished(endTime);
    }
}
