package com.micro.im.req;

import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-14 12:30:12
 */
@Data
public class AddFriendReq {
    private Long friend;
    private String remark;
    private Long friendgroup;
}
