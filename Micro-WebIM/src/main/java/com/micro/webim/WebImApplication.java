package com.micro.webim;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mr.zxb
 * @date 2020-06-10 20:02:09
 */
@SpringBootApplication
@MapperScan(value = "com.micro.webim.mapper")
public class WebImApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebImApplication.class, args);
    }
}
