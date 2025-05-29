package tech.zhiwei.frostmetal.modules.mydata.mail;

import tech.zhiwei.frostmetal.system.mail.MailSender;
import tech.zhiwei.tool.date.DateUtil;
import tech.zhiwei.tool.lang.AssertUtil;
import tech.zhiwei.tool.lang.StringUtil;
import tech.zhiwei.tool.lang.Validator;

/**
 * MyData 邮件通知
 *
 * @author LIEN
 * @since 2025/5/8
 */
public class MyDataMail {
    /**
     * 流水线 [执行失败] 的通知邮件
     */
    public static void notifyPipelineFailed(String email, String projectName, String pipelineName) {
        AssertUtil.isTrue(Validator.isEmail(email), "邮箱无效！");
        AssertUtil.isTrue(StringUtil.isNotEmpty(projectName), "项目名称无效");
        AssertUtil.isTrue(StringUtil.isNotEmpty(pipelineName), "流水线名称无效");

        String subject = "MyData - 流水线执行失败";
        String content = StringUtil.format("项目【{}】中的流水线【{}】于 {} 执行失败，详见执行日志。", projectName, pipelineName, DateUtil.now());
        MailSender.sendHtml(email, subject, content);
    }

    /**
     * 流水线 [执行成功] 的通知邮件
     */
    public static void notifyPipelineSuccess(String email, String projectName, String pipelineName) {
        AssertUtil.isTrue(Validator.isEmail(email), "邮箱无效！");
        AssertUtil.isTrue(StringUtil.isNotEmpty(projectName), "项目名称无效");
        AssertUtil.isTrue(StringUtil.isNotEmpty(pipelineName), "流水线名称无效");

        String subject = "MyData - 流水线执行成功";
        String content = StringUtil.format("项目【{}】中的流水线【{}】于 {} 执行成功。", projectName, pipelineName, DateUtil.now());
        MailSender.sendHtml(email, subject, content);
    }
}