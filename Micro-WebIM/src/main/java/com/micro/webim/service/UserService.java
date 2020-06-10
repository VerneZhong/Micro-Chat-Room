package com.micro.webim.service;

import com.micro.webim.entity.User;
import com.micro.webim.request.UserRegisterReq;
import com.micro.webim.resp.GetListResp;
import com.micro.webim.resp.GetMembersResp;

/**
 * user interface
 *
 * @author Mr.zxb
 * @date 2020-06-10 12:33
 */
public interface UserService {
    /**
     * 获取用户list
     * @param userId
     * @return
     */
    GetListResp getList(Long userId);

    /**
     * 获取群员列表
     * @param groupId 群组ID
     * @return
     */
    GetMembersResp getMembers(Long groupId);

    /**
     * 注册用户
     * @param req
     */
    void register(UserRegisterReq req);

    /**
     * 账号是否已存在
     * @param account
     * @return
     */
    boolean accountExists(String account);

    /**
     * 用户登录
     * @param account
     * @param password
     * @return
     */
    User login(String account, String password);
}
