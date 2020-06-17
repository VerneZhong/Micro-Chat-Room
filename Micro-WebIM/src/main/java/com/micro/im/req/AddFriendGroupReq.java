package com.micro.im.req;

import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-17 14:08
 */
@Data
public class AddFriendGroupReq {
    private Long userId;
    private String friendGroupName;
}
