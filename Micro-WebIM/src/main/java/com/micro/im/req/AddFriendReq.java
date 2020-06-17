package com.micro.im.req;

import lombok.Data;

/**
 * 添加好友请求
 * @author Mr.zxb
 * @date 2020-06-14 12:30:12
 */
@Data
public class AddFriendReq {
    private Long uid;
    private Long friend;
    private String remark;
    private Long friendgroup;
}
