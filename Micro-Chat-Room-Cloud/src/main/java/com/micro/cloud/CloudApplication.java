package com.micro.cloud;

import com.micro.thrift.user.UserService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * 业务云端服务引导
 * @author Mr.zxb
 * @date 2020-05-16 19:39:40
 */
@SpringBootApplication
@MapperScan(value = "com.micro.cloud.mapper")
@Slf4j
public class CloudApplication {

    @Value("${thrift.user.port}")
    private int serverPort;

    @Autowired
    private UserService.Iface userService;

    public static void main(String[] args) {
        SpringApplication.run(CloudApplication.class, args);
    }

    @PostConstruct
    public void initThriftUserServer() {
        try {
            TProcessor processor = new UserService.Processor<>(userService);
            InetSocketAddress address = new InetSocketAddress(serverPort);
            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(address);
            TNonblockingServer.Args args = new TNonblockingServer.Args(serverSocket);
            args.processor(processor);

            args.transportFactory(new TFramedTransport.Factory());
            args.protocolFactory(new TBinaryProtocol.Factory());

            TServer tServer = new TNonblockingServer(args);
            log.info("User-ThriftService-启动成功，端口：{}", serverPort);
            tServer.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
            log.info("User-ThriftService-启动失败，异常信息：{}", e.getMessage());
        }
    }
}
