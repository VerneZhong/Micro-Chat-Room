package com.micro.im.service;

import com.micro.common.constant.FileType;
import com.micro.common.constant.ServerConstant;
import com.micro.common.util.FileUtil;
import com.micro.im.resp.UploadFileResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-12 09:28
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Override
    public UploadFileResp uploadFile(MultipartFile file, FileType fileType) {
        String originalFilename = file.getOriginalFilename();
        log.info("文件上传：{}", originalFilename);
        if (originalFilename != null) {
            String fileName = StringUtils.cleanPath(originalFilename);
            try {
                if (fileName.contains("..")) {
                    throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
                }
                String path = getPath(fileType);
                String baseDir = getResourceBasePath() + path;
                if (!FileUtil.exists(baseDir)) {
                    FileUtil.create(baseDir);
                }
                Path fileStorageLocation = Paths.get(baseDir).toAbsolutePath().normalize();
                Path targetLocation = fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                return new UploadFileResp(path + fileName, fileName);
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }
        throw new RuntimeException("文件不存在 " + file);
    }

    /**
     * 获取项目根路径
     *
     * @return
     */
    private static String getResourceBasePath() {
        // 获取跟目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:static/").getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (path == null || !path.exists()) {
            path = new File("");
        }
        String pathStr = path.getAbsolutePath();
        pathStr = pathStr.replace("\\target\\classes", "");

        return pathStr;
    }

    private String getPath(FileType fileType) {
        switch (fileType) {
            case IMG:
                return ServerConstant.UPLOAD_IMG_PATH;
            case FILE:
                return ServerConstant.UPLOAD_FILE_PATH;
            case VIDEO:
                return ServerConstant.UPLOAD_VIDEO_PATH;
            default:
                return ServerConstant.BASE_UPLOAD;
        }
    }
}
