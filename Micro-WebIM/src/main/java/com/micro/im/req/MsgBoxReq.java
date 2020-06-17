package com.micro.im.req;

import lombok.Data;

/**
 * 获取消息盒子
 *
 * @author Mr.zxb
 * @date 2020-06-17 14:38
 */
@Data
public class MsgBoxReq {
    private Integer page;
    private Long userId;
}
