package com.micro.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.micro.common.dto.UserDTO;
import com.micro.im.configuration.RedisClient;
import com.micro.im.entity.UserFriends;
import com.micro.im.mapper.UserFriendsMapper;
import com.micro.thrift.user.UserThriftService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Mr.zxb
 * @date 2020-06-17 19:49:41
 */
@Service
@Slf4j
public class UserThriftServiceImpl implements UserThriftService.Iface {

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private UserService userService;

    @Override
    public void setUserOffline(long userId, String status) throws TException {
        log.info("更新用户在线状态：{} --- {}", userId, status);
        redisClient.set(String.valueOf(userId), status);
    }

    @Override
    public long getUserIdByToken(String token) throws TException {
        log.info("通过token：{} 获取用户ID信息", token);
        UserDTO userDTO = (UserDTO) redisClient.get(token);
        return Optional.ofNullable(userDTO).map(UserDTO::getId).orElse(0L);
    }

    @Override
    public List<Long> getFriendByUserId(long userId) throws TException {
        log.info("获取好友列表：{}", userId);
        List<UserFriends> userFriends = userService.getUserFriends(userId);
        if (!CollectionUtils.isEmpty(userFriends)) {
            return userFriends.stream().map(UserFriends::getFriendId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
