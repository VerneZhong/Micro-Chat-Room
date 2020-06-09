package com.micro.im.base;

import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-09 21:05:29
 */
@Data
public class ResponseVO<T> {
    private String code;
    private String msg;
    private T data;
}
