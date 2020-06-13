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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.micro.common.util.FileUtil.SEPARATOR;

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
                String targetPath = getTargetResourceBasePath() + path;
                if (!FileUtil.exists(targetPath)) {
                    FileUtil.create(targetPath);
                }
                copyFile(file.getInputStream(), fileName, baseDir);
                copyFile(file.getInputStream(), fileName, targetPath);
                return new UploadFileResp(path + fileName, fileName);
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException("文件不存在 " + file);
    }

    /**
     * 拷贝一个文件流到目标目录
     * @param is
     * @param fileName
     * @param baseDir
     */
    private void copyFile(InputStream is, String fileName, String baseDir) {
        try {
            Path fileStorageLocation = Paths.get(baseDir).toAbsolutePath().normalize();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(is, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取实际项目根路径
     *
     * @return
     */
    private String getResourceBasePath() throws FileNotFoundException {
        // 获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if (!path.exists()) {
            path = new File("");
        }
        String pathStr = path.getAbsolutePath();
        pathStr = pathStr.replace(SEPARATOR + "target" + SEPARATOR + "classes", "");
        pathStr += SEPARATOR +"src" + SEPARATOR + "main" + SEPARATOR + "resources" + SEPARATOR + "static";
        return pathStr;
    }

    /**
     * 获取target项目根路径
     *
     * @return
     */
    private String getTargetResourceBasePath() throws FileNotFoundException {
        // 获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:static/").getPath());
        if (!path.exists()) {
            path = new File("");
        }
        return path.getAbsolutePath();
    }

    /**
     * 上传文件类型
     *
     * @param fileType
     * @return
     */
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
