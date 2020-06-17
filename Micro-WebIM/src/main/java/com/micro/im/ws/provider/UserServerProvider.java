package com.micro.im.ws.provider;

import com.micro.thrift.user.UserThriftService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import static com.micro.common.constant.ServerConstant.THRIFT_USER_PORT;
import static com.micro.common.constant.ServerConstant.SERVER_HOST;

/**
 * User RPC Server Provider
 * @author Mr.zxb
 * @date 2020-06-17 20:26:06
 */
public class UserServerProvider {

    public static UserThriftService.Client getUserService() {
        TSocket socket = new TSocket(SERVER_HOST, THRIFT_USER_PORT, 5000);
        TTransport transport = new TFramedTransport(socket);
        try {
            transport.open();
        } catch (TTransportException e) {
            e.printStackTrace();
            return null;
        }
        TProtocol protocol = new TBinaryProtocol(transport);
        return new UserThriftService.Client(protocol);
    }
}
