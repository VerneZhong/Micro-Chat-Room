package com.micro.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.micro.common.dto.SetFriendStatusMessage;
import com.micro.common.protocol.IMP;
import com.micro.common.util.DateUtil;
import com.micro.common.util.MD5Util;
import com.micro.im.configuration.RedisClient;
import com.micro.im.entity.*;
import com.micro.im.mapper.*;
import com.micro.im.req.*;
import com.micro.im.resp.GetListResp;
import com.micro.im.resp.GetMembersResp;
import com.micro.im.resp.MsgBoxResp;
import com.micro.im.vo.*;
import com.micro.im.ws.WsServer;
import com.micro.im.ws.dto.AddFriendMessage;
import com.micro.im.ws.dto.SystemMessage;
import com.micro.im.ws.upstream.BaseMessageData;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.micro.common.constant.ServerConstant.HIDE;
import static com.micro.common.constant.ServerConstant.ONLINE;
import static com.micro.im.ws.WsServer.CLIENT_MAP;

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

    @Autowired
    private UserFriendsMapper userFriendsMapper;

    @Autowired
    private MessageBoxMapper messageBoxMapper;

    /**
     * 获取用户list
     *
     * @param userId
     * @return
     */
    @Override
    public GetListResp getList(Long userId) {
        // 当前用户
        Mine mine = getMine(userId);

        // 查询分组好友列表
        LambdaQueryWrapper<UserFriendsGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFriendsGroup::getUserId, userId);
        List<UserFriendsGroup> userFriendsGroups = userFriendsGroupMapper.selectList(lambdaQueryWrapper);

        // 查询我的好友列表
        LambdaQueryWrapper<UserFriends> friendsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        friendsLambdaQueryWrapper.eq(UserFriends::getUserId, userId);
        List<UserFriends> userFriends = userFriendsMapper.selectList(friendsLambdaQueryWrapper);
        Map<Long, List<UserFriends>> userFriendsMap =
                userFriends.stream().collect(Collectors.groupingBy(UserFriends::getGroupId));

        List<Long> friendIds = userFriends.stream()
                .map(UserFriends::getFriendId)
                .collect(Collectors.toList());

        List<FriendGroup> friendGroups = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(userFriendsGroups)) {
            friendGroups = userFriendsGroups.stream()
                    .map(userFriendsGroup -> {
                        FriendGroup friendGroup = new FriendGroup();
                        friendGroup.setGroupname(userFriendsGroup.getName());
                        friendGroup.setId(userFriendsGroup.getId());
                        friendGroup.setList(Lists.newArrayList());
                        return friendGroup;
                    })
                    .collect(Collectors.toList());
        }
        if (!CollectionUtils.isEmpty(friendIds)) {
            // 查询该用户的好友列表
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getId, friendIds);
            List<User> friends = userMapper.selectList(userLambdaQueryWrapper);

            Map<Long, List<User>> userListMap = friends.stream().collect(Collectors.groupingBy(User::getId));
            friendGroups = userFriendsGroups.stream()
                    .map(userFriendsGroup -> getFriendGroup(userListMap, userFriendsGroup, userFriendsMap))
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

    private Mine getMine(Long userId) {
        Mine mine = new Mine();
        User user = userMapper.selectById(userId);
        mine.setId(String.valueOf(userId));
        mine.setUsername(user.getNickname());
        String status = (String) redisClient.get(userId.toString());
        mine.setStatus(ONLINE.equalsIgnoreCase(status) ? status : HIDE);
        mine.setAvatar(user.getAvatarAddress());
        mine.setSign(user.getSign());
        return mine;
    }

    private GroupVO transformGroupVO(Group group) {
        GroupVO groupVO = new GroupVO();
        groupVO.setGroupname(group.getName());
        groupVO.setId(group.getId().toString());
        groupVO.setAvatar(group.getAvatar());
        return groupVO;
    }

    private FriendGroup getFriendGroup(Map<Long, List<User>> userListMap,
                                       UserFriendsGroup userFriendsGroup,
                                       Map<Long, List<UserFriends>> userFriendsMap) {
        FriendGroup friendGroup = new FriendGroup();
        friendGroup.setGroupname(userFriendsGroup.getName());
        friendGroup.setId(userFriendsGroup.getId());

        List<UserFriends> userFriends = userFriendsMap.get(userFriendsGroup.getId());
        if (CollectionUtils.isEmpty(userFriends)) {
            friendGroup.setList(Lists.newArrayList());
            return friendGroup;
        }
        List<Long> friendsList = userFriends.stream()
                .map(UserFriends::getFriendId)
                .collect(Collectors.toList());

        List<FriendVO> friendVOS = Lists.newArrayList();
        for (Long friendId : friendsList) {
            List<User> users = userListMap.get(friendId);
            List<FriendVO> friendVOList = users.stream()
                    .map(friend -> getFriendVO(friend))
                    .collect(Collectors.toList());
            friendVOS.addAll(friendVOList);
        }
        friendGroup.setList(friendVOS);
        return friendGroup;
    }

    private FriendVO getFriendVO(User friend) {
        FriendVO friendVO = new FriendVO();
        String id = friend.getId().toString();
        friendVO.setId(id);
        friendVO.setUsername(friend.getNickname());
        friendVO.setAvatar(friend.getAvatarAddress());
        friendVO.setSign(friend.getSign());
        String online = (String) redisClient.get(id);
        friendVO.setStatus(online != null ? online : "offline");
        return friendVO;
    }

    /**
     * 获取群员列表
     *
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
     *
     * @param req
     */
    @Override
    public void register(UserRegisterReq req) {
        User user = new User();
        user.setAccount(req.getAccount());
        user.setPassword(MD5Util.md5(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        // 默认头像
        user.setAvatarAddress("/image/photo/qq.png");
        user.setRegisterDate(LocalDate.now());
        user.setIsLocked(0);
        userMapper.insert(user);

        UserFriendsGroup userFriendsGroup = new UserFriendsGroup();
        userFriendsGroup.setName("我的好友");
        userFriendsGroup.setUserId(user.getId());
        userFriendsGroupMapper.insert(userFriendsGroup);
    }

    /**
     * 账号是否已存在
     *
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
     *
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

    @Override
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Override
    public List<User> getRecommend(Long mineId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.notIn(User::getId, mineId);
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public List<User> findUserByAccountAndName(String val, Integer limit) {
        List<User> result = Lists.newArrayList();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (limit != null) {
            Page<User> userPage = new Page<>();
            userPage.setCurrent(limit);
            queryWrapper.like(User::getAccount, val);
            Page<User> page = userMapper.selectPage(userPage, queryWrapper);
            result.addAll(page.getRecords());
            queryWrapper.clear();
            queryWrapper.like(User::getNickname, val);
            List<User> records = userMapper.selectPage(userPage, queryWrapper).getRecords();
            result.addAll(records);
        } else {
            queryWrapper.like(User::getAccount, val);
            List<User> users = userMapper.selectList(queryWrapper);
            result.addAll(users);
            queryWrapper.clear();
            queryWrapper.like(User::getNickname, val);
            List<User> users2 = userMapper.selectList(queryWrapper);
            result.addAll(users2);
        }
        HashMap<Long, User> map = Maps.newHashMap();
        for (User user : result) {
            map.putIfAbsent(user.getId(), user);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * 发送添加好友请求
     *
     * @param req
     */
    @Override
    public void sendAddFriendReq(AddFriendReq req) throws Exception {
        // 发送有消息盒子ws消息
        sendMessageBox(req.getFriend(), IMP.ADD_FRIEND);
        // 是否已有申请的消息
        int type = 1;
        LambdaQueryWrapper<MessageBox> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageBox::getType, type)
                .eq(MessageBox::getForm, req.getUid())
                .eq(MessageBox::getTo, req.getFriend())
                .eq(MessageBox::getStatus, 1);

        Integer selectCount = messageBoxMapper.selectCount(queryWrapper);
        if (selectCount == 0) {
            // 将消息存入到mysql中
            insertMessage(req.getUid(), req.getFriend(), req.getFriendgroup(), req.getRemark(), type, 1);
        }
    }

    private void sendMessageBox(Long friend, IMP imp) throws Exception {
        // 判断好友是否在线
        String status = getUserStatus(friend);
        if (ONLINE.equals(status)) {
            // 发送ws消息给好友
            Channel channel = CLIENT_MAP.get(friend);
            BaseMessageData<Long> messageData = new BaseMessageData<>();
            messageData.setType(imp.getName());
            messageData.setData(friend);
            log.info("send messagebox friend ws req:{}", messageData);
            WsServer.getInstance().sendMessage(channel, messageData);
        }
    }

    /**
     * 将未读消息存入mysql
     *
     * @param form          消息发送者
     * @param to            消息接收者
     * @param friendGroupId 好友分组
     * @param remark        备注
     * @param type          消息类型
     * @param status        消息状态
     */
    private void insertMessage(Long form, Long to, Long friendGroupId, String remark, int type, int status) {
        MessageBox message = new MessageBox();
        message.setType(type);
        message.setForm(form);
        message.setTo(to);
        message.setFriendGroupId(friendGroupId);
        message.setRemark(remark);
        message.setStatus(status);
        message.setSendTime(DateUtil.getNow());
        message.setReadTime(DateUtil.getNow());

        log.info("新增消息：{}", message);
        messageBoxMapper.insert(message);
    }

    /**
     * 添加好友分组
     *
     * @param req
     */
    @Override
    public void addFriendGroup(AddFriendGroupReq req) {
        UserFriendsGroup friendsGroup = new UserFriendsGroup();
        friendsGroup.setUserId(req.getUserId());
        friendsGroup.setName(req.getFriendGroupName());
        userFriendsGroupMapper.insert(friendsGroup);
    }

    /**
     * 好友分组是否存在
     *
     * @param userId
     * @param name
     * @return
     */
    @Override
    public boolean friendGroupExists(Long userId, String name) {
        LambdaQueryWrapper<UserFriendsGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriendsGroup::getUserId, userId)
                .eq(UserFriendsGroup::getName, name);
        Integer count = userFriendsGroupMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 获取消息盒子消息
     *
     * @param req
     * @return
     */
    @Override
    public List<MsgBoxResp> getMessageBox(MsgBoxReq req) {
        LambdaQueryWrapper<MessageBox> lambdaQueryWrapper = new LambdaQueryWrapper<MessageBox>()
                .eq(MessageBox::getTo, req.getUserId())
                .or()
                .eq(MessageBox::getForm, req.getUserId());
        Page<MessageBox> page = new Page<>();
        page.setCurrent(req.getPage());
        Page<MessageBox> messageBoxPage = messageBoxMapper.selectPage(page, lambdaQueryWrapper);

        return messageBoxPage.getRecords().stream()
                .map(this::transformMessageBox)
                .collect(Collectors.toList());
    }

    /**
     * 获取消息盒子消息数量
     *
     * @param userId
     * @return
     */
    @Override
    public Integer getMessageBoxCount(Long userId) {
        LambdaQueryWrapper<MessageBox> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MessageBox::getTo, userId);
        lambdaQueryWrapper.eq(MessageBox::getStatus, 1);
        return messageBoxMapper.selectCount(lambdaQueryWrapper);
    }

    /**
     * 将消息设置为已读
     *
     * @param userId
     */
    @Override
    public void setMessageRead(Long userId) {
        LambdaQueryWrapper<MessageBox> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MessageBox::getTo, userId);
        MessageBox messageBox = new MessageBox();
        messageBox.setStatus(7);
        messageBoxMapper.update(messageBox, lambdaQueryWrapper);
    }

    /**
     * 确认添加好友
     *
     * @param req
     */
    @Override
    public void confirmAddFriend(ConfirmAddFriendReq req) throws Exception {
        // 发送消息给好友，已同意添加其好友
        if (ONLINE.equals(getUserStatus(req.getFriend()))) {
            // 发送ws消息给好友
            Channel channel = CLIENT_MAP.get(req.getFriend());
            BaseMessageData<AddFriendMessage> messageData = new BaseMessageData<>();
            AddFriendMessage friendMessage = new AddFriendMessage();
            Mine mine = getMine(req.getUid());
            friendMessage.setId(req.getUid());
            friendMessage.setUsername(mine.getUsername());
            friendMessage.setAvatar(mine.getAvatar());
            friendMessage.setType(IMP.FRIEND.getName());
            friendMessage.setSign(mine.getSign());
            friendMessage.setGroupid(req.getFromGroup());
            messageData.setType(IMP.CONFIRM_ADD_FRIEND.getName());
            messageData.setData(friendMessage);
            log.info("send add friend ws req:{}", messageData);
            WsServer.getInstance().sendMessage(channel, messageData);
        }
        LambdaQueryWrapper<MessageBox> boxLambdaQueryWrapper = new LambdaQueryWrapper<>();
        boxLambdaQueryWrapper.eq(MessageBox::getType, 2)
                .eq(MessageBox::getTo, req.getFriend())
                .eq(MessageBox::getForm, req.getUid());
        Integer count = messageBoxMapper.selectCount(boxLambdaQueryWrapper);
        if (count == 0) {
            // 新增系统消息
            insertMessage(req.getFriend(), req.getUid(), null, null, 2, 2);
        }

        // 更新该消息状态
        MessageBox messageBox = new MessageBox();
        messageBox.setId(req.getMessageId());
        messageBox.setStatus(2);
        messageBox.setReadTime(DateUtil.getNow());
        messageBoxMapper.updateById(messageBox);

        // 是否已添加过好友
        LambdaQueryWrapper<UserFriends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriends::getUserId, req.getUid())
                .eq(UserFriends::getFriendId, req.getFriend());
        Integer selectCount = userFriendsMapper.selectCount(queryWrapper);
        if (selectCount == 0) {
            UserFriends userFriends = new UserFriends();
            userFriends.setUserId(req.getUid());
            userFriends.setFriendId(req.getFriend());
            userFriends.setGroupId(req.getGroup());
            userFriendsMapper.insert(userFriends);
        }

        queryWrapper.clear();
        queryWrapper.eq(UserFriends::getUserId, req.getFriend())
                .eq(UserFriends::getFriendId, req.getUid());
        selectCount = userFriendsMapper.selectCount(queryWrapper);
        if (selectCount == 0) {
            UserFriends friends = new UserFriends();
            friends.setUserId(req.getFriend());
            friends.setFriendId(req.getUid());
            friends.setGroupId(req.getFromGroup());
            userFriendsMapper.insert(friends);
        }
    }

    @Override
    public void refuseFriend(RefuseFriendReq req) throws Exception {
        sendMessageBox(req.getFrom(), IMP.REFUSE_FRIEND);
        // 新增拒绝添加好友的系统消息
        insertMessage(req.getFrom(), req.getTo(), null, null, 2, 3);

        MessageBox messageBox = new MessageBox();
        messageBox.setId(req.getMessageId());
        messageBox.setStatus(3);
        messageBox.setReadTime(DateUtil.getNow());
        messageBoxMapper.updateById(messageBox);
    }

    @Override
    public void sendUserStatusMessage(Long userId, String status) throws Exception {
        // 发送在线消息给好友
        List<Long> friends = getUserFriends(userId).stream()
                .map(UserFriends::getFriendId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(friends)) {
            List<Channel> channels = friends.stream().map(CLIENT_MAP::get).collect(Collectors.toList());
            for (Channel channel : channels) {
                // 发送好友在线状态
                WsServer.getInstance().sendMessage(channel, getSetFriendStatusMessageData(userId, status));

                WsServer.getInstance().sendMessage(channel, getSystemMessageData(userId));
            }
        }
    }

    private BaseMessageData<SystemMessage> getSystemMessageData(Long userId) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setId(userId);
        systemMessage.setSystem(true);
        systemMessage.setType(IMP.FRIEND.getName());
        systemMessage.setContent("对方已掉线");
        BaseMessageData<SystemMessage> systemBaseMessageData = new BaseMessageData<>();
        systemBaseMessageData.setType(IMP.SYSTEM.getName());
        systemBaseMessageData.setData(systemMessage);
        return systemBaseMessageData;
    }

    private BaseMessageData<SetFriendStatusMessage> getSetFriendStatusMessageData(Long userId, String status) {
        SetFriendStatusMessage message = new SetFriendStatusMessage();
        message.setId(userId);
        message.setStatus(status);
        BaseMessageData<SetFriendStatusMessage> baseMessageData = new BaseMessageData<>();
        baseMessageData.setData(message);
        baseMessageData.setType(IMP.SET_FRIEND_STATUS.getName());
        return baseMessageData;
    }

    @Override
    public List<UserFriends> getUserFriends(Long userId) {
        LambdaQueryWrapper<UserFriends> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFriends::getUserId, userId);
        return userFriendsMapper.selectList(queryWrapper);
    }

    @Override
    public UserFriendsGroup addMyGroup(AddMyGroupReq req) {
        UserFriendsGroup friendsGroup = new UserFriendsGroup();
        friendsGroup.setName(req.getGroupName());
        friendsGroup.setUserId(req.getUserId());
        userFriendsGroupMapper.insert(friendsGroup);
        return friendsGroup;
    }

    @Override
    public void editGroupName(EditGroupNameReq req) {
        UserFriendsGroup friendsGroup = new UserFriendsGroup();
        friendsGroup.setId(req.getGroupId());
        friendsGroup.setName(req.getGroupName());
        userFriendsGroupMapper.updateById(friendsGroup);
    }

    private String getUserStatus(Long friend) {
        return (String) redisClient.get(friend.toString());
    }

    private MsgBoxResp transformMessageBox(MessageBox box) {
        MsgBoxResp resp = new MsgBoxResp();
        resp.setMsgIdx(box.getId());
        resp.setFrom(box.getForm());
        resp.setTo(box.getTo());
        resp.setMsgType(box.getType());
        resp.setFriendGroupId(box.getFriendGroupId());
        resp.setSendTime(box.getSendTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        resp.setRemark(box.getRemark());
        resp.setStatus(box.getStatus());
        resp.setReadTime(Optional.ofNullable(box.getReadTime()).map(date -> date.format(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"))).orElse(""));
        resp.setFromInfo(getMine(box.getForm()));
        resp.setToInfo(getMine(box.getTo()));
        return resp;
    }
}
