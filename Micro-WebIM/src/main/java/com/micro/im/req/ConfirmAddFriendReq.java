package com.micro.im.req;

import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-17 21:38:20
 */
@Data
public class ConfirmAddFriendReq {
    private Long uid;
    private Long fromGroup;
    private Long group;
}
