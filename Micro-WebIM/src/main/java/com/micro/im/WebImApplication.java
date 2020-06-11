package com.micro.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author Mr.zxb
 * @date 2020-06-10 20:02:09
 */
@SpringBootApplication
@MapperScan(value = "com.micro.im.mapper")
@ServletComponentScan
public class WebImApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebImApplication.class, args);
    }
}
