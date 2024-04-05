package org.springblade.modules.mydata.job.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.data.BizDataDAO;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.manage.mail.MailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * 任务的邮件处理类
 *
 * @author LIEN
 * @since 2024/4/5
 */
@Component
@Slf4j
public class JobEmailService {

    @Resource
    private BizDataDAO bizDataDAO;

    /**
     * 发送消费数据的邮件
     *
     * @param taskInfo  任务
     * @param excelFile Excel数据文件
     */
    public void sendMail(TaskInfo taskInfo, File excelFile) {
        String emailAddress = taskInfo.getConsumeEmail();
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress
                    , StrUtil.format("[MyData] 任务 [{}] 推送数据", taskInfo.getTaskName())
                    , StrUtil.format("任务[{}]推送数据，请查看附件。", taskInfo.getTaskName())
                    , excelFile);
            log.info("email messageId = {}", messageId);
        }
    }

}
