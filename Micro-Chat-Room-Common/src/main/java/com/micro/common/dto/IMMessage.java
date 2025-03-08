package com.micro.common.dto;

import lombok.Data;
import org.msgpack.annotation.Message;

/**
 * 消息实体
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
@Message
@Data
public class IMMessage {

    /**
     * IP 地址及端口
     */
    private String addr;
    /**
     * 命令类型 login/system/logout
     */
    private String cmd;
    /**
     * 发送命令时间
     */
    private long time;
    /**
     * 当前在线人数
     */
    private int online;
    /**
     * 发送者
     */
    private String sender;
    /**
     * 接收者
     */
    private String receiver;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 终端
     */
    private String terminal;

    public IMMessage() {
    }

    public IMMessage(String cmd, long time, int online, String content) {
        this.cmd = cmd;
        this.time = time;
        this.online = online;
        this.content = content;
    }

    public IMMessage(String cmd, String terminal, long time, String sender) {
        this.cmd = cmd;
        this.time = time;
        this.sender = sender;
        this.terminal = terminal;
    }

    public IMMessage(String cmd, long time, String sender, String content) {
        this.cmd = cmd;
        this.time = time;
        this.sender = sender;
        this.content = content;
    }

    @Override
    public String toString() {
        return "IMMessage{" +
                "addr='" + addr + '\'' +
                ", cmd='" + cmd + '\'' +
                ", time=" + time +
                ", online=" + online +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", terminal='" + terminal + '\'' +
                '}';
    }
}
