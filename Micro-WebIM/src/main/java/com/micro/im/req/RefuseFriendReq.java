package com.micro.im.req;

import lombok.Data;

/**
 * 拒绝好友添加 req
 *
 * @author Mr.zxb
 * @date 2020-07-02 10:15
 */
@Data
public class RefuseFriendReq {
    private Long to;
    private Long from;
    private Long messageId;
}
