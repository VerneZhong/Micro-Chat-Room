package com.micro.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-07-02 10:05
 */
public class DateUtil {

    public static LocalDateTime getNow() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }
}
