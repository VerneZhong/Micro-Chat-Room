package com.micro.im.req;

import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-07-03 15:56
 */
@Data
public class AddMyGroupReq {
    private Long userId;
    private String groupName;
}
