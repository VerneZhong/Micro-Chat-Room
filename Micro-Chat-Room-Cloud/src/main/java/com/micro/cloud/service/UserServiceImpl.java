package com.micro.cloud.service;

import com.micro.cloud.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Mr.zxb
 * @date 2020-06-04 22:03:38
 */
@Service
public class UserServiceImpl implements UserService {

    @Override
    public Optional<User> login(String account, String password) {
        return null;
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void deleteUser(Integer userID) {

    }
}
