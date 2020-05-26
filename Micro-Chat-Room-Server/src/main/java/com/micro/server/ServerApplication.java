package com.micro.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.annotation.PostConstruct;

/**
 * 服务端引导类
 * @author Mr.zxb
 * @date 2020-05-20 19:05:39
 */
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @PostConstruct
    public void startServer() {
        NettyServer nettyServer = NettyServer.getInstance();
        nettyServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(nettyServer::stop));
    }
}
