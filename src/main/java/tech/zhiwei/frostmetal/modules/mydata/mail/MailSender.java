package tech.zhiwei.frostmetal.modules.mydata.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tech.zhiwei.tool.spring.SpringUtil;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 发送系统邮件
 *
 * @author LIEN
 * @since 2024/12/11
 */
@Slf4j
public class MailSender {
    private static final MailConfig mailConfig;

    private static final MailAccount mailAccount;

    static {
        mailConfig = SpringUtil.getBean(MailConfig.class);
        mailAccount = new MailAccount();
        mailAccount.setHost(mailConfig.getHost());
        mailAccount.setAuth(true);
        mailAccount.setFrom(mailConfig.getFrom());
        mailAccount.setUser(mailConfig.getUser());
        mailAccount.setPass(mailConfig.getPass());
        mailAccount.setSslEnable(mailConfig.getSsl());

        startMailConsumer();
    }

    // 待发送邮件的队列，用于逐个发送
    private static final BlockingQueue<MailTask> mailQueue = new LinkedBlockingQueue<>();

    private static synchronized void startMailConsumer() {
        Thread consumerThread = new Thread(() -> {
            while (true) {
                try {
                    MailTask mailTask = mailQueue.take();
                    doSend(mailTask);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("发送邮件异常", e);
                }
            }
        });
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    /**
     * 实际发送邮件
     *
     * @param task 邮件任务
     */
    private static void doSend(MailTask task) {
        MailUtil.send(mailAccount, task.getTo(), task.getSubject(), task.getContent(), task.isHtml(), task.getAttachments());
    }

    /**
     * 发送系统邮件
     *
     * @param to      接收人邮件地址
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     */
    public static void sendHtml(String to, String subject, String content, File... files) {
        MailTask mailTask = new MailTask();
        mailTask.setTo(to);
        mailTask.setSubject(subject);
        mailTask.setContent(content);
        mailTask.setAttachments(files);
        mailTask.setHtml(true);
        mailQueue.offer(mailTask);
    }
}

/**
 * 邮件Bean
 */
@Data
class MailTask {
    private String to;
    private String subject;
    private String content;
    private File[] attachments;
    private boolean isHtml;
}