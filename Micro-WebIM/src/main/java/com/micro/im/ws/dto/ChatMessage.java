package com.micro.im.ws.dto;

import lombok.Data;

/**
 * 聊天消息
 *
 * @author Mr.zxb
 * @date 2020-06-12 15:45
 */
@Data
public class ChatMessage {
    /**
     * 发送者用户名
     */
    private String username;
    /**
     * 发送者头像
     */
    private String avatar;
    /**
     * 发送者ID（私聊则是用户ID，群聊则是群组ID）
     */
    private String id;
    /**
     * 聊天窗口类型，从发送者消息传递到to里面获取，（一般为私聊friend和群聊group）
     */
    private String type;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息id，可不传。除非对消息进行操作（如撤回）
     */
    private Integer cid;
    /**
     * 是否是我发送的消息，
     */
    private boolean mine;
    /**
     * 消息的发送者ID（或是群组中的某个消息发送者）
     */
    private String fromid;
    /**
     * 当前时间戳毫秒数
     */
    private Long timestamp;
}
