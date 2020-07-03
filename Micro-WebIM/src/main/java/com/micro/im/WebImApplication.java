package com.micro.im;

import com.micro.im.ws.WsServer;
import com.micro.thrift.user.UserThriftService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static com.micro.common.constant.ServerConstant.THRIFT_USER_PORT;

/**
 * @author Mr.zxb
 * @date 2020-06-10 20:02:09
 */
@SpringBootApplication
@MapperScan(value = "com.micro.im.mapper")
@ServletComponentScan
@Slf4j
public class WebImApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebImApplication.class, args);
    }

    @PostConstruct
    public void startServer() throws Exception {
        WsServer ws = WsServer.getInstance();
        ws.start();
        Runtime.getRuntime().addShutdownHook(new Thread(ws::stop));
        // 异步启动 Thrift Server
        CompletableFuture.runAsync(this::initThriftUserServer);
    }

    @Autowired
    private UserThriftService.Iface userService;

    public void initThriftUserServer() {
        try {
            TProcessor processor = new UserThriftService.Processor<>(userService);
            InetSocketAddress address = new InetSocketAddress(THRIFT_USER_PORT);
            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(address);
            TNonblockingServer.Args args = new TNonblockingServer.Args(serverSocket);
            args.processor(processor);
            args.transportFactory(new TFramedTransport.Factory());
            args.protocolFactory(new TBinaryProtocol.Factory());
            TServer tServer = new TNonblockingServer(args);
            log.info("User-ThriftService-启动成功，端口：{}", THRIFT_USER_PORT);
            tServer.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
            log.info("User-ThriftService-启动失败，异常信息：{}", e.getMessage());
        }
    }
}
