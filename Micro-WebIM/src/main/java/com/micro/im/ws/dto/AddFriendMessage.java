package com.micro.im.ws.dto;

import lombok.Data;

/**
 * 添加好友 消息
 *
 * @author Mr.zxb
 * @date 2020-06-12 16:14
 */
@Data
public class AddFriendMessage {

    /**
     * friend
     */
    private String type;
    /**
     * 好友头像
     */
    private String avatar;
    /**
     * 好友昵称
     */
    private String username;
    /**
     * 好友分组ID
     */
    private Long groupid;
    /**
     * 好友ID
     */
    private String id;
    /**
     * 好友签名
     */
    private String sign;
}
