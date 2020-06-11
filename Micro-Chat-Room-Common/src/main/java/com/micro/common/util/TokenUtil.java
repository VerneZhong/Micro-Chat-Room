package com.micro.common.util;

import java.util.Random;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-10-27 17:11
 */
public class TokenUtil {

    public static String getToken() {
        return randomCode("0123456789abcdefghijklmnopqrstuvwsyz", 32);
    }

    public static String randomCode(String s, int size) {
        StringBuilder stringBuilder = new StringBuilder(size);

        Random random = new Random();
        for (int i = 0; i < size; i++) {
            int loc = random.nextInt(s.length());
            stringBuilder.append(s.charAt(loc));
        }
        return stringBuilder.toString();
    }
}
