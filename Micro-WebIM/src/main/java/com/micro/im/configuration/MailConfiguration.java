package com.micro.im.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author Mr.zxb
 * @date 2020-06-15 19:28:46
 */
@Configuration
public class MailConfiguration {
    @Value("${mail.smtp.host}")
    private String host;
    @Value("${mail.smtp.username}")
    private String username;
    @Value("${mail.smtp.password}")
    private String password;
    @Value("${mail.smtp.defaultEncoding}")
    private String encoding;
    @Value("${mail.smtp.auth}")
    private String auth;
    @Value("${mail.smtp.timeout}")
    private String timeout;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setPort(587);
        mailSender.setDefaultEncoding(encoding);
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", auth);
        properties.put("mail.smtp.timeout", timeout);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        mailSender.setJavaMailProperties(properties);
        return mailSender;
    }
}
