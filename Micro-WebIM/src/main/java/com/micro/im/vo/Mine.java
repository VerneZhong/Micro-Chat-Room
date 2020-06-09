package com.micro.im.vo;

import lombok.Data;

/**
 * 自己 vo
 * @author Mr.zxb
 * @date 2020-06-09 20:47:17
 */
@Data
public class Mine {

    /**
     * 昵称
     */
    private String username;

    /**
     * 用户id
     */
    private String id;

    /**
     * 在线状态
     */
    private String status;

    /**
     * 签名
     */
    private String sign;

    /**
     * 头像
     */
    private String avatar;
}
