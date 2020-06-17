package com.micro.im.service;

import com.micro.im.configuration.RedisClient;
import com.micro.thrift.user.UserThriftService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.zxb
 * @date 2020-06-17 19:49:41
 */
@Service
@Slf4j
public class UserThriftServiceImpl implements UserThriftService.Iface {

    @Autowired
    private RedisClient redisClient;

    @Override
    public void setUserOffline(long userId, String status) throws TException {
        log.info("更新用户在线状态：{} --- {}", userId, status);
        redisClient.set(String.valueOf(userId), status);
    }
}
