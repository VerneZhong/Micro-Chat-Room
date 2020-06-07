package com.micro.test;

import com.micro.server.provider.ServerProvider;
import com.micro.thrift.user.UserInfo;
import com.micro.thrift.user.UserService;
import org.apache.thrift.TException;

/**
 * @author Mr.zxb
 * @date 2020-06-07 19:55:21
 */
public class ThriftUserTest {

    public static void main(String[] args) throws TException {

        ServerProvider serverProvider = new ServerProvider();

        UserService.Client userService = serverProvider.getUserService();

        UserInfo userById = userService.getUserById(1);

        System.out.println(userById);
    }
}
