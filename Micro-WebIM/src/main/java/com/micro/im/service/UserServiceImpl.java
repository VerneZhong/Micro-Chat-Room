package com.micro.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.micro.common.util.MD5Util;
import com.micro.im.configuration.RedisClient;
import com.micro.im.entity.Group;
import com.micro.im.entity.User;
import com.micro.im.entity.UserFriendsGroup;
import com.micro.im.entity.UserGroupRelation;
import com.micro.im.mapper.GroupMapper;
import com.micro.im.mapper.UserFriendsGroupMapper;
import com.micro.im.mapper.UserGroupRelationMapper;
import com.micro.im.mapper.UserMapper;
import com.micro.im.request.UserRegisterReq;
import com.micro.im.resp.GetListResp;
import com.micro.im.resp.GetMembersResp;
import com.micro.im.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-10 12:37
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFriendsGroupMapper userFriendsGroupMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserGroupRelationMapper userGroupRelationMapper;

    /**
     * 获取用户list
     * @param userId
     * @return
     */
    @Override
    public GetListResp getList(Long userId) {
        Mine mine = new Mine();
        User user = userMapper.selectById(userId);
        mine.setId(String.valueOf(userId));
        mine.setUsername(user.getNickname());
        mine.setStatus((String) redisClient.get(userId.toString()));
        mine.setAvatar(user.getAvatarAddress());
        mine.setSign(user.getSign());

        LambdaQueryWrapper<UserFriendsGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFriendsGroup::getUserId, userId);
        List<UserFriendsGroup> userFriendsGroups = userFriendsGroupMapper.selectList(lambdaQueryWrapper);

        List<Long> friendIds = userFriendsGroups.stream()
                .map(UserFriendsGroup::getUserId)
                .collect(Collectors.toList());

        List<FriendGroup> friendGroups = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(friendIds)) {
            // 查询该用户的好友列表
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getId, friendIds);
            List<User> friends = userMapper.selectList(userLambdaQueryWrapper);

            Map<Long, List<User>> userListMap = friends.stream().collect(Collectors.groupingBy(User::getId));
            friendGroups = userFriendsGroups.stream()
                    .map(userFriendsGroup -> getFriendGroup(userListMap, userFriendsGroup))
                    .collect(Collectors.toList());
        }
        // 查找用户加入的群组
        List<GroupVO> groupVOS = Lists.newArrayList();
        LambdaQueryWrapper<UserGroupRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupRelation::getUserId, userId);
        List<UserGroupRelation> userGroupRelations = userGroupRelationMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(userGroupRelations)) {
            List<Long> groupIds = userGroupRelations.stream()
                    .map(UserGroupRelation::getGroupId).collect(Collectors.toList());
            LambdaQueryWrapper<Group> groupLambdaQueryWrapper = new LambdaQueryWrapper<>();
            groupLambdaQueryWrapper.in(Group::getId, groupIds);
            List<Group> groups = groupMapper.selectList(groupLambdaQueryWrapper);
            groupVOS = groups.stream().map(this::transformGroupVO).collect(Collectors.toList());
        }

        // 返回最终数据
        GetListResp resp = new GetListResp();
        resp.setMine(mine);
        resp.setFriend(friendGroups);
        resp.setGroup(groupVOS);
        return resp;
    }

    private GroupVO transformGroupVO(Group group) {
        GroupVO groupVO = new GroupVO();
        groupVO.setGroupname(group.getName());
        groupVO.setId(group.getId().toString());
        groupVO.setAvatar(group.getAvatar());
        return groupVO;
    }

    private FriendGroup getFriendGroup(Map<Long, List<User>> userListMap, UserFriendsGroup userFriendsGroup) {
        FriendGroup friendGroup = new FriendGroup();
        friendGroup.setGroupname(userFriendsGroup.getName());
        friendGroup.setId(userFriendsGroup.getId());
        List<User> users = userListMap.get(userFriendsGroup.getUserId());
        List<FriendVO> friendVOS = users.stream().map(friend -> getFriendVO(friend))
                .collect(Collectors.toList());
        friendGroup.setList(friendVOS);
        return friendGroup;
    }

    private FriendVO getFriendVO(User friend) {
        FriendVO friendVO = new FriendVO();
        String  id = friend.getId().toString();
        friendVO.setId(id);
        friendVO.setUsername(friend.getNickname());
        friendVO.setAvatar(friend.getAvatarAddress());
        friendVO.setSign(friend.getSign());
        String online = (String) redisClient.get(id);
        friendVO.setStatus(online);
        return friendVO;
    }

    /**
     * 获取群员列表
     * @param groupId 群组ID
     * @return
     */
    @Override
    public GetMembersResp getMembers(Long groupId) {
        LambdaQueryWrapper<UserGroupRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupRelation::getGroupId, groupId);
        List<Long> userIds = userGroupRelationMapper.selectList(queryWrapper).stream()
                        .map(UserGroupRelation::getUserId).collect(Collectors.toList());

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userLambdaQueryWrapper);

        List<MembersVO> membersVOS = users.stream().map(user -> {
            MembersVO vo = new MembersVO();
            vo.setUsername(user.getNickname());
            vo.setId(user.getId().toString());
            vo.setAvatar(user.getAvatarAddress());
            vo.setSign(user.getSign());
            return vo;
        }).collect(Collectors.toList());

        GetMembersResp resp = new GetMembersResp();
        resp.setList(membersVOS);
        return resp;
    }

    /**
     * 注册用户
     * @param req
     */
    @Override
    public void register(UserRegisterReq req) {
        User user = new User();
        user.setAccount(req.getAccount());
        user.setPassword(MD5Util.md5(req.getPassword()));
        user.setNickname(req.getNickname());
        // 默认头像
        user.setAvatarAddress("/image/photo/qq.png");
        user.setRegisterDate(LocalDate.now());
        user.setIsLocked(0);
        userMapper.insert(user);
    }

    /**
     * 账号是否已存在
     * @param account
     * @return true -> 已存在
     */
    @Override
    public boolean accountExists(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        Integer count = userMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 用户登录
     * @param account
     * @param password
     * @return
     */
    @Override
    public User login(String account, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        queryWrapper.eq(User::getPassword, MD5Util.md5(password));
        List<User> users = userMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(users)) {
            return users.get(0);
        }
        return null;
    }
}
