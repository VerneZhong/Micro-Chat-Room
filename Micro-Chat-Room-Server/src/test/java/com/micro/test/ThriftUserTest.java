package com.micro.test;

import com.micro.server.provider.ServerProvider;
import com.micro.thrift.user.UserInfo;
import com.micro.thrift.user.UserService;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;



/**
 * @author Mr.zxb
 * @date 2020-06-07 19:55:21
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = ThriftUserTest.class)
public class ThriftUserTest {

//    @Autowired
//    private ServerProvider serverProvider;

//    @Test
//    public void testGetUserById() throws TException {
//
//        UserService.Client userService = serverProvider.getUserService();
//
//        UserInfo userById = userService.getUserById(1);
//
//        System.out.println(userById);
//    }

    public static void main(String[] args) throws TException {
        ServerProvider serverProvider = new ServerProvider();
        UserService.Client userService = serverProvider.getUserService();

        UserInfo user = new UserInfo();
        user.setAccount("zhongxb");
        user.setPassword("123456");
        user.setAge(29);
        user.setAddress("北京");
        user.setNickname("大师兄");
        userService.registerUser(user);

//        UserInfo userById = userService.getUserById(1);
//        System.out.println(userById);
    }
}
