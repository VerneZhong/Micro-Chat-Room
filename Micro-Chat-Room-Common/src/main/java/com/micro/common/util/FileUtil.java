package com.micro.common.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-12 14:00
 */
public class FileUtil {

    public static final String SEPARATOR = File.separator;

    public static boolean exists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public static void create(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
