package com.micro.im.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.im.entity.MessageBox;
import com.micro.im.entity.User;
import com.micro.im.entity.UserFriends;
import com.micro.im.entity.UserFriendsGroup;
import com.micro.im.req.*;
import com.micro.im.resp.GetListResp;
import com.micro.im.resp.GetMembersResp;
import com.micro.im.resp.MsgBoxResp;
import com.micro.im.vo.Mine;

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
     * @throws Exception
     */
    void sendAddFriendReq(AddFriendReq addFriendReq) throws Exception;

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

    /**
     * 将消息设置为已读
     * @param userId
     */
    void setMessageRead(Long userId);

    /**
     * 确认添加好友
     * @param req
     */
    void confirmAddFriend(ConfirmAddFriendReq req) throws Exception;

    /**
     * 拒绝好友添加
     * @param req
     */
    void refuseFriend(RefuseFriendReq req) throws Exception;

    /**
     * 发送给好友，在线状态
     * @param userId
     * @param status
     */
    void sendUserStatusMessage(Long userId, String status) throws Exception;

    /**
     * 获取好友列表
     * @param userId
     * @return
     */
    List<UserFriends> getUserFriends(Long userId);

    /**
     * 添加我的好友分组
     * @param req
     * @return
     */
    UserFriendsGroup addMyGroup(AddMyGroupReq req);

    /**
     * 编辑分组名称
     * @param req
     * @return
     */
    void editGroupName(EditGroupNameReq req);

    /**
     * 删除好友分组
     * @param id
     */
    void deleteFriendGroup(Long id);

    /**
     * 获取默认好友分组id
     * @param userId
     * @return
     */
    Long getDefaultFriendGroup(Long userId);

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    Mine getMine(Long userId);

    /**
     * 移动好友至新分组
     * @param userId
     * @param to
     * @param from
     */
    void moveFriendGroup(Long userId, Long to, Long from);

    /**
     * 修改好友备注
     * @param userId
     * @param friendId
     * @param nickName
     */
    void editFriendRemark(Long userId, Long friendId, String nickName);
}
