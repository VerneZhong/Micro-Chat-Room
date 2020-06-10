package com.micro.cloud.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.micro.cloud.configuration.RedisClient;
import com.micro.cloud.entity.Group;
import com.micro.cloud.entity.User;
import com.micro.cloud.entity.UserFriendsGroup;
import com.micro.cloud.entity.UserGroupRelation;
import com.micro.cloud.mapper.GroupMapper;
import com.micro.cloud.mapper.UserFriendsGroupMapper;
import com.micro.cloud.mapper.UserGroupRelationMapper;
import com.micro.cloud.mapper.UserMapper;
import com.micro.cloud.resp.GetListResp;
import com.micro.cloud.resp.GetMembersResp;
import com.micro.cloud.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

        // 查询该用户的好友列表
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.in(User::getId, friendIds);
        List<User> friends = userMapper.selectList(userLambdaQueryWrapper);

        Map<Long, List<User>> userListMap = friends.stream().collect(Collectors.groupingBy(User::getId));
        List<FriendGroup> friendGroups = userFriendsGroups.stream()
                .map(userFriendsGroup -> getFriendGroup(userListMap, userFriendsGroup))
                .collect(Collectors.toList());

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
}
