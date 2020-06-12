package com.micro.common.util;

import java.io.IOException;
import java.nio.file.*;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-12 14:00
 */
public class FileUtil {

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
