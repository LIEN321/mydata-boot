package org.springblade.modules.mydata.job.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.job.bean.TaskJob;
import org.springblade.modules.mydata.manage.mail.MailSender;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 任务的邮件处理类
 *
 * @author LIEN
 * @since 2024/4/5
 */
@Component
@AllArgsConstructor
@Slf4j
public class JobEmailService {

    /**
     * 发送消费数据的邮件
     *
     * @param taskJob   任务
     * @param excelFile Excel数据文件
     */
    public void sendConsumeData(TaskJob taskJob, File excelFile, String emailAddress) {
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress, StrUtil.format("{} 推送数据", taskJob.getTaskName()), StrUtil.format("任务[{}]向您推送数据，请查看附件。", taskJob.getTaskName()), excelFile);
            log.info("email messageId = {}", messageId);
        }
    }

    /**
     * 发送过滤数据的邮件
     *
     * @param taskJob   任务
     * @param excelFile Excel数据文件
     */
    public void sendFilteredData(TaskJob taskJob, File excelFile, String emailAddress) {
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress, StrUtil.format("{} 数据过滤通知", taskJob.getTaskName()), StrUtil.format("时间：{}，任务【{}】因部分数据不符合过滤条件被拦截，请查看附件。", DateUtil.now(), taskJob.getTaskName()), excelFile);
            log.info("email messageId = {}", messageId);
        }
    }

    public void sendFailedNotice(TaskJob taskJob, String emailAddress) {
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress, StrUtil.format("{} 异常通知", taskJob.getTaskName()), StrUtil.format("时间：{}，任务【{}】异常，异常信息请详见任务日志。", DateUtil.now(), taskJob.getTaskName()));
            log.info("email messageId = {}", messageId);
        }
    }
}
