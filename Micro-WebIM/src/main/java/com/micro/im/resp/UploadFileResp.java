package com.micro.im.resp;

import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-12 09:18
 */
@Data
public class UploadFileResp {
    private String src;
    private String name;

    public UploadFileResp(String src, String name) {
        this.src = src;
        this.name = name;
    }
}
