package org.springblade.modules.mydata.job.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.mydata.job.bean.TaskInfo;
import org.springblade.modules.mydata.manage.mail.MailSender;
import org.springblade.modules.system.entity.UserInfo;
import org.springblade.modules.system.service.IUserService;
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
@AllArgsConstructor
@Slf4j
public class JobEmailService {
    @Resource
    private final IUserService userService;

    /**
     * 发送消费数据的邮件
     *
     * @param taskInfo  任务
     * @param excelFile Excel数据文件
     */
    public void sendConsumeData(TaskInfo taskInfo, File excelFile) {
        String emailAddress = taskInfo.getConsumeEmail();
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress
                    , StrUtil.format("{} 推送数据", taskInfo.getTaskName())
                    , StrUtil.format("任务[{}]向您推送数据，请查看附件。", taskInfo.getTaskName())
                    , excelFile);
            log.info("email messageId = {}", messageId);
        }
    }

    /**
     * 发送过滤数据的邮件
     *
     * @param taskInfo  任务
     * @param excelFile Excel数据文件
     */
    public void sendFilteredData(TaskInfo taskInfo, File excelFile) {
        UserInfo userInfo = userService.userInfo(taskInfo.getCreateUser());
        String emailAddress = userInfo.getUser().getEmail();
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress
                    , StrUtil.format("数据过滤通知", taskInfo.getTaskName())
                    , StrUtil.format("时间：{}，任务【{}】因部分数据不符合过滤条件被拦截，请查看附件。", DateUtil.now(), taskInfo.getTaskName())
                    , excelFile);
            log.info("email messageId = {}", messageId);
        }
    }

    public void sendFailedNotice(TaskInfo taskInfo) {
        // 查询创建人的邮件
        UserInfo userInfo = userService.userInfo(taskInfo.getCreateUser());
        String emailAddress = userInfo.getUser().getEmail();
        if (StrUtil.isNotBlank(emailAddress)) {
            String messageId = MailSender.sendMail(emailAddress
                    , StrUtil.format("任务异常停止", taskInfo.getTaskName())
                    , StrUtil.format("任务【{}】异常停止，时间：{}，异常信息请详见任务日志。", taskInfo.getTaskName(), DateUtil.now()));
            log.info("email messageId = {}", messageId);
        }
    }
}
