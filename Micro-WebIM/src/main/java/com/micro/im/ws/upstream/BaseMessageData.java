package com.micro.im.ws.upstream;

import com.micro.common.util.JsonUtil;

/**
 * 公共的消息类
 * @author Mr.zxb
 * @date 2020-06-21 11:04:50
 */
public class BaseMessageData<T> {

    private String type;
    private T data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
