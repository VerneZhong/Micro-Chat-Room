package com.wechat.cloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 业务云端服务引导
 * @author Mr.zxb
 * @date 2020-05-16 19:39:40
 */
@SpringBootApplication
@MapperScan(value = "com.wechat.cloud.mapper")
public class CloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudApplication.class, args);
    }
}
