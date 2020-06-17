package com.micro.im.service;

import com.micro.im.configuration.EmailComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *
 * @author Mr.zxb
 * @date 2020-06-15 19:27:17
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailComponent emailComponent;

    /**
     * todo 未实现
     * @param mobile 手机号码
     * @param message 手机信息
     * @return
     */
    @Override
    public boolean sendMobileMessage(String mobile, String message) {
        log.info("Message-Service send mobile: {} message: {}", mobile, message);
        return true;
    }

    @Override
    public boolean sendEmailMessage(String email, String message) {
        log.info("Message-Service send email: {} message: {}", email, message);
        return emailComponent.sendMail(message, "Micro-WebIM团队邮件", email, javaMailSender, false);
    }
}
