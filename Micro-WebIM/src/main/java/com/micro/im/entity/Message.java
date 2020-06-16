package com.micro.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author Mr.zxb
 * @since 2020-06-15
 */
@Data
public class Message extends Model<Message> {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息类型（1为请求添加用户消息）；2为系统消息（添加好友）；3为请求加群消息；4为系统消息（添加群系统消息）；5为全体用户消息
     */
    private Integer type;

    /**
     * 消息发送者（0表示为系统消息）
     */
    private Long form;

    /**
     * 消息接收者（0位全体用户）
     */
    private Long to;

    /**
     * 1未读，2同意，3拒绝，4同意且返回消息已读，5拒绝且返回消息已读，6全体消息已读
     */
    private Integer status;

    /**
     * 附加消息
     */
    private String remark;

    /**
     * 发送消息时间
     */
    @TableField("sendTime")
    private LocalDateTime sendTime;

    /**
     * 读取消息时间
     */
    @TableField("readTime")
    private LocalDateTime readTime;

    /**
     * 接收消息的管理员
     */
    @TableField("adminGroup")
    private Long adminGroup;

    /**
     * 处理该请求的管理员ID
     */
    private Long handler;

    /**
     * 好友分组
     */
    private Long friendGroupid;

    @Override
    protected Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "Message{" +
        ", id=" + id +
        ", type=" + type +
        ", form=" + form +
        ", to=" + to +
        ", status=" + status +
        ", remark=" + remark +
        ", sendTime=" + sendTime +
        ", readTime=" + readTime +
        ", adminGroup=" + adminGroup +
        ", handler=" + handler +
        ", friendGroupid=" + friendGroupid +
        "}";
    }
}
