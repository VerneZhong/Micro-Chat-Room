package com.micro.im.service;

/**
 * 消息发送接口，发送邮件或短信
 * @author Mr.zxb
 * @date 2020-06-15 19:25:37
 */
public interface MessageService {

    /**
     * 发送手机短信
     * @param mobile 手机号码
     * @param message 手机信息
     * @return
     */
    boolean sendMobileMessage(String mobile, String message);

    /**
     * 发送邮件
     * @param email 邮件地址
     * @param message 邮件信息
     * @return
     */
    boolean sendEmailMessage(String email, String message);
}
