package tech.zhiwei.frostmetal.modules.mydata.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import tech.zhiwei.tool.spring.SpringUtil;

import java.io.File;

/**
 * 发送系统邮件
 *
 * @author LIEN
 * @since 2024/12/11
 */
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
    }

    /**
     * 发送系统邮件
     *
     * @param to      接收人邮件地址
     * @param subject 主题
     * @param content 内容
     * @param files   附件列表
     * @return 邮件id
     */
    public static String sendMail(String to, String subject, String content, File... files) {
        return MailUtil.send(mailAccount, to, subject, content, true, files);
    }
}
