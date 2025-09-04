package tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.email;

import tech.zhiwei.frostmetal.modules.mydata.mail.MailSender;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineLog;
import tech.zhiwei.frostmetal.modules.mydata.manage.entity.PipelineTask;
import tech.zhiwei.frostmetal.modules.mydata.schedule.pipeline.executor.TaskExecutor;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.map.MapUtil;

import java.io.File;
import java.util.Map;

/**
 * 发送邮件
 *
 * @author LIEN
 * @since 2024/12/11
 */
public class SendEmail extends TaskExecutor {
    public SendEmail(PipelineTask pipelineTask, PipelineLog pipelineLog) {
        super(pipelineTask, pipelineLog);
    }

    @Override
    public void doExecute(Map<String, Object> jobContextData) {
        PipelineTask pipelineTask = getPipelineTask();
        Map<String, String> emailConfig = (Map<String, String>) pipelineTask.getTaskConfig().get("EMAIL");
        if (MapUtil.isEmpty(emailConfig)) {
//            error("发送邮件失败，缺少邮件配置");
            throw new IllegalArgumentException("发送邮件失败，缺少邮件配置");
        }

        String address = emailConfig.get("ADDRESS");
        if (StringUtil.isEmpty(address)) {
//            error("发送邮件失败，收件人地址无效");
            throw new IllegalArgumentException("发送邮件失败，收件人地址无效");
        }

        String subject = emailConfig.get("SUBJECT");
        if (StringUtil.isEmpty(subject)) {
//            error("发送邮件失败，邮件主题无效");
            throw new IllegalArgumentException("发送邮件失败，邮件主题无效");
        }

        String content = emailConfig.get("CONTENT");
        if (StringUtil.isEmpty(content)) {
//            error("发送邮件失败，邮件内容无效");
            throw new IllegalArgumentException("发送邮件失败，邮件内容无效");
        }

        String fileKey = emailConfig.get("FILE");
        File file = null;
        if (StringUtil.isNotEmpty(fileKey)) {
            file = (File) jobContextData.get(fileKey);
            if (file == null) {
                error("发送失败，前置任务没有生成文件，变量名为{}", fileKey);
            }
        }

        log("发送邮件开始");
        MailSender.sendHtml(address, subject, content, file);
        log("发送邮件结束");
    }
}
