package com.micro.im.service;

import com.micro.common.constant.FileType;
import com.micro.im.resp.UploadFileResp;
import org.springframework.web.multipart.MultipartFile;

/**
 * user interface
 *
 * @author Mr.zxb
 * @date 2020-06-12 09:23
 */
public interface FileService {

    /**
     * 上传文件
     * @param file
     * @param fileType
     * @return
     */
    UploadFileResp uploadFile(MultipartFile file, FileType fileType);

}
