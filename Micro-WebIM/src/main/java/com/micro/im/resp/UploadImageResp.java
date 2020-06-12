package com.micro.im.resp;

import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-12 09:18
 */
@Data
public class UploadImageResp {
    private String src;

    public UploadImageResp(String src) {
        this.src = src;
    }
}
