package com.micro.cloud.service;

import com.micro.cloud.entity.User;

import java.util.Optional;

/**
 * @author Mr.zxb
 * @date 2020-06-04 22:01:17
 */
public interface UserService {

    /**
     * 用户登录接口
     * @param account
     * @param password
     * @return
     */
    Optional<User> login(String account, String password);

    /**
     * 新增用户
     * @param user
     */
    void addUser(User user);

    /**
     * 更新用户
     * @param user
     */
    void update(User user);

    /**
     * 删除用户
     * @param userID
     */
    void deleteUser(Integer userID);
}
