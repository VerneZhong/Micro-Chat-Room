package com.micro.cloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.micro.cloud.entity.User;
import com.micro.cloud.mapper.UserMapper;
import com.micro.common.util.MD5Util;
import com.micro.thrift.user.UserInfo;
import com.micro.thrift.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author Mr.zxb
 * @date 2020-06-04 22:03:38
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService.Iface {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserInfo getUserById(int id) throws TException {
        User user = userMapper.selectById(id);
        if (user != null) {
            return transformUser(user);
        }
        return null;
    }

    private UserInfo transformUser(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setAccount(user.getAccount());
        userInfo.setNickname(user.getNickname());
        userInfo.setAge(user.getAge());
        userInfo.setAvatar(user.getAvatarAddress());
        userInfo.setAddress(user.getArea());
        return userInfo;
    }

    @Override
    public UserInfo login(String account, String password) throws TException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account).eq("password", password);
        User user = userMapper.selectOne(wrapper);
        if (user != null) {
            return transformUser(user);
        }
        return null;
    }

    @Override
    public void registerUser(UserInfo userInfo) throws TException {
        User user = new User();
        user.setAccount(userInfo.getAccount());
        user.setPassword(MD5Util.md5(userInfo.getPassword()));
        user.setNickname(userInfo.getNickname());
//        user.setCellphoneNumber();
        user.setAvatarAddress(userInfo.getAvatar());
        user.setAge(userInfo.getAge());
        user.setArea(userInfo.getAddress());
        user.setIsLocked(0);
        user.setRegisterDate(LocalDate.now());

        userMapper.insert(user);
    }

    @Override
    public boolean isLock(String account) throws TException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        User user = userMapper.selectOne(wrapper);
        if (user != null) {
            return user.getIsLocked();
        }
        return false;
    }

    @Override
    public boolean accountExists(String account) throws TException {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        User user = userMapper.selectOne(wrapper);
        return user != null;
    }
}
