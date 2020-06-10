package com.micro.cloud;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

//    @Autowired
//    private UserService.Iface userService;

    public static void main(String[] args) {
        SpringApplication.run(CloudApplication.class, args);
    }

//    @PostConstruct
//    public void initThriftUserServer() {
//        try {
//            TProcessor processor = new UserService.Processor<>(userService);
//            InetSocketAddress address = new InetSocketAddress(serverPort);
//            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(address);
//            TNonblockingServer.Args args = new TNonblockingServer.Args(serverSocket);
//            args.processor(processor);
//
//            args.transportFactory(new TFramedTransport.Factory());
//            args.protocolFactory(new TBinaryProtocol.Factory());
//
//            TServer tServer = new TNonblockingServer(args);
//            log.info("User-ThriftService-启动成功，端口：{}", serverPort);
//            tServer.serve();
//        } catch (TTransportException e) {
//            e.printStackTrace();
//            log.info("User-ThriftService-启动失败，异常信息：{}", e.getMessage());
//        }
//    }
}
