package com.micro.client;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.annotation.PostConstruct;

/**
 * 客户端引导类
 *
 * @author Mr.zxb
 * @date 2020-05-16 19:33:11
 */
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @PostConstruct
    public void start() {
        NettyClient client = NettyClient.getInstance("zhongxb");
        client.start();

        Runtime.getRuntime().addShutdownHook(new Thread(client::stop));
    }
}
