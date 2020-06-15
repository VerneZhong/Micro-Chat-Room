package com.micro.im.configuration;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Mr.zxb
 * @date 2020-06-15 19:27:56
 */
@Component
public class EmailComponent {
    /**
     *
     * Text或者HTML格式邮件的方法
     *
     * @param text
     *            要发送的内容
     * @param subject
     *            邮件的主题也就是邮件的标题
     * @param emailAddress
     *            目的地
     * @param javaMailSender
     *            发送邮件的核心类（在xml文件中已经配置好了）
     * @param type
     *            如果为true则代表发送HTML格式的文本
     * @return
     */
    public boolean sendMail(String text, String subject, String emailAddress,
                            JavaMailSender javaMailSender, Boolean type) {
        MimeMessage mMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mMessageHelper;
        Properties prop = new Properties();
        try {
            // 从配置文件中拿到发件人邮箱地址
            prop.load(this.getClass().getResourceAsStream("/application.yml"));
            String from = prop.get("username") + "";
            mMessageHelper = new MimeMessageHelper(mMessage, true, "UTF-8");
            // 发件人邮箱
            mMessageHelper.setFrom(from);
            // 收件人邮箱
            mMessageHelper.setTo(emailAddress);
            // 邮件的主题也就是邮件的标题
            mMessageHelper.setSubject(subject);
            // 邮件的文本内容，true表示文本以html格式打开
            if (type) {
                mMessageHelper.setText(text, true);
            } else {
                mMessageHelper.setText(text, false);
            }
            // 发送邮件
            javaMailSender.send(mMessage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
