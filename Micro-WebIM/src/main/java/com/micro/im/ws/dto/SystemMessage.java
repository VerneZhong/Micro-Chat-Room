package com.micro.im.ws.dto;

import com.micro.common.protocol.IMP;
import lombok.Data;

/**
 * 系统消息
 *
 * @author Mr.zxb
 * @date 2020-06-12 15:56
 */
@Data
public class SystemMessage {

    /**
     * 系统消息，true
     */
    private boolean system;
    /**
     * 聊天窗口ID
     */
    private Long id;
    /**
     * 聊天窗口类型（friend/group）{@link IMP#FRIEND} {@link IMP#GROUP}
     */
    private String type;
    /**
     * 消息内容
     */
    private String content;
}
