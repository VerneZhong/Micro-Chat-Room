package com.micro.webim.vo;

import lombok.Data;

/**
 * 文件上传 VO
 * @author Mr.zxb
 * @date 2020-06-09 21:08:39
 */
@Data
public class UploadFileVO {
    /**
     * 文件地址
     */
    private String src;
    /**
     * 文件名
     */
    private String name;
}
