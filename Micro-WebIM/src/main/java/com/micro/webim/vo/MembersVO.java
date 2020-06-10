package com.micro.webim.vo;

import lombok.Data;

/**
 * 群员 VO
 * @author Mr.zxb
 * @date 2020-06-09 21:01:56
 */
@Data
public class MembersVO {
    /**
     * 群员昵称
     */
    private String username;
    /**
     * 群员id
     */
    private String id;
    /**
     * 群员头像地址
     */
    private String avatar;
    /**
     * 群员签名
     */
    private String sign;
}
