package com.micro.im.req;

import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-07-03 16:18
 */
@Data
public class EditGroupNameReq {
    private Long groupId;
    private String groupName;
}
