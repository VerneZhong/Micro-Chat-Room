package com.micro.common.dto;

import lombok.Data;
import org.msgpack.annotation.Message;

/**
 * @author Mr.zxb
 * @date 2020-05-28 20:01:53
 */
@Data
@Message
public class UserDTO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 地址
     */
    private String address;

}
