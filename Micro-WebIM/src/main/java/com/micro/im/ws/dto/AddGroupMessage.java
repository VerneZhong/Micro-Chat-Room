package com.micro.im.ws.dto;

import lombok.Data;

/**
 * 添加群 消息
 *
 * @author Mr.zxb
 * @date 2020-06-12 16:14
 */
@Data
public class AddGroupMessage {

    /**
     * friend
     */
    private String type;
    /**
     * 群组头像
     */
    private String avatar;
    /**
     * 群组名称
     */
    private String groupname;
    /**
     * 群组ID
     */
    private String id;
}
