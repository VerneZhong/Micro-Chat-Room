package com.micro.im.req;

import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-17 21:38:20
 */
@Data
public class ConfirmAddFriendReq {
    /**
     * 消息id
     */
    private Long messageId;
    /**
     * 用户id
     */
    private Long uid;
    /**
     *  对方设定的好友分组
     */
    private Long fromGroup;
    /**
     * 当前用户设定的分组
     */
    private Long group;
    /**
     * 好友id
     */
    private Long friend;
}
