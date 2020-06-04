package com.micro.cloud.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户登录
 * @author Mr.zxb
 * @date 2020-06-04 22:00:13
 */
@Data
public class UserLoginReq {
    @NotNull
    private String account;
    @NotNull
    private String password;
}
