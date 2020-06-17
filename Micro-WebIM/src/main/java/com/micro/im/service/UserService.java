package com.micro.im.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.im.entity.MessageBox;
import com.micro.im.entity.User;
import com.micro.im.req.AddFriendGroupReq;
import com.micro.im.req.AddFriendReq;
import com.micro.im.req.MsgBoxReq;
import com.micro.im.req.UserRegisterReq;
import com.micro.im.resp.GetListResp;
import com.micro.im.resp.GetMembersResp;
import com.micro.im.resp.MsgBoxResp;

import java.util.List;

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

    /**
     * 修改用户信息
     * @param user
     */
    void updateUser(User user);

    /**
     * 获取好友推荐列表
     * @param mineId
     * @return
     */
    List<User> getRecommend(Long mineId);

    /**
     * 根据账号查找好友
     * @param account
     * @param limit
     * @return
     */
    List<User> findUserByAccountAndName(String account, Integer limit);

    /**
     * 发送添加好友请求
     * @param addFriendReq
     */
    void sendAddFriendReq(AddFriendReq addFriendReq);

    /**
     * 添加好友分组
     * @param req
     */
    void addFriendGroup(AddFriendGroupReq req);

    /**
     * 好友分组是否存在
     * @param userId
     * @param name
     * @return
     */
    boolean friendGroupExists(Long userId, String name);

    /**
     * 获取消息盒子消息
     * @param req
     * @return
     */
    List<MsgBoxResp> getMessageBox(MsgBoxReq req);

    /**
     * 获取消息盒子消息数量
     * @param userId
     * @return
     */
    Integer getMessageBoxCount(Long userId);
}
