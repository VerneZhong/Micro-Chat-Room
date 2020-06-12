package com.micro.im;

import com.micro.im.ws.WsServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;

/**
 * @author Mr.zxb
 * @date 2020-06-10 20:02:09
 */
@SpringBootApplication
@MapperScan(value = "com.micro.im.mapper")
@ServletComponentScan
public class WebImApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebImApplication.class, args);
    }

    @PostConstruct
    public void startServer() throws Exception {
        WsServer ws = WsServer.getInstance();
        ws.start();
        Runtime.getRuntime().addShutdownHook(new Thread(ws::stop));
    }
}
