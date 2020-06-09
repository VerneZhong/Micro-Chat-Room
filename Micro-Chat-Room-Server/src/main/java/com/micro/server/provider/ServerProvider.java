package com.micro.server.provider;

import com.micro.common.constant.ServerConstant;
import com.micro.thrift.user.UserService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 服务提供者
 *
 * @author Mr.zxb
 * @date 2020-06-07 18:01:00
 */
@Component
public class ServerProvider {

    @Value("${thrift.user.ip}")
    private String serverIp;
    @Value("${thrift.user.port}")
    private int serverPort;

    public UserService.Client getUserService() {
        TSocket socket;
        if (serverIp != null) {
            socket = new TSocket(serverIp, serverPort, 5000);
        } else {
            socket = new TSocket(ServerConstant.SERVER_HOST, ServerConstant.THRIFT_USER_PORT, 5000);
        }
        TTransport transport = new TFramedTransport(socket);
        try {
            transport.open();
        } catch (TTransportException e) {
            e.printStackTrace();
            return null;
        }
        return new UserService.Client(new TBinaryProtocol(transport));
    }
}
