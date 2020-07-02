package com.micro.im.ws.handler;

import com.micro.common.dto.ChatMessage;
import com.micro.common.dto.SetFriendStatusMessage;
import com.micro.common.protocol.IMP;
import com.micro.common.util.JsonUtil;
import com.micro.im.ws.WsServer;
import com.micro.im.ws.downstream.DownstreamMessageData;
import com.micro.im.ws.dto.SystemMessage;
import com.micro.im.ws.provider.UserServerProvider;
import com.micro.im.ws.upstream.BaseMessageData;
import com.micro.thrift.user.UserThriftService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.micro.common.constant.ServerConstant.OFFLINE;
import static com.micro.common.constant.ServerConstant.ONLINE;
import static com.micro.im.ws.WsServer.*;

/**
 * ws handler
 *
 * @author Mr.zxb
 * @date 2020-06-12 16:52
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 记录在线的客户端
     */
    private final ChannelGroup channelGroup;

    private final Map<Long, Channel> channelMap;

    public WebSocketServerHandler(ChannelGroup channelGroup, Map<Long, Channel> channelMap) {
        this.channelGroup = channelGroup;
        this.channelMap = channelMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 和服务器建立连接
//        channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        log.info("ws message: {}", text);
        String type = JsonUtil.getRootValueByKey(text, "type");
        if (StringUtils.isNotBlank(type)) {
            switch (type) {
                case "login":
                    // 发送上线消息
                    Optional<String> optional = Optional.ofNullable(JsonUtil.getRootValueByKey(text, "data"));
                    if (optional.isPresent()) {
                        String token = optional.get();
                        long userId = getUserIdByToken(token);
                        ctx.channel().attr(USER_ID_KEY).getAndSet(userId);
                        // 设置好友在线状态
                        setUserStatus(userId, ONLINE);
                        if (channelGroup.find(ctx.channel().id()) == null) {
                            channelGroup.add(ctx.channel());
                        }
                        channelMap.putIfAbsent(userId, ctx.channel());
                    }
                    break;
                case "chat":
                    sendChatMessage(text, ctx.channel());
                    break;
                case "system":

                    break;
                case "logout":

                    break;
                case "groupMessage":

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置用户在线状态
     *
     * @param userId
     * @param status
     * @throws Exception
     */
    private void setUserStatus(long userId, String status) throws Exception {
        // 设置用户在线状态
        log.info("set user online status: {}", userId);
        UserThriftService.Client userService = UserServerProvider.getUserService();
        Objects.requireNonNull(userService);
        userService.setUserOffline(userId, status);

        // 发送在线消息给好友
        List<Long> friends = userService.getFriendByUserId(userId);
        if (!CollectionUtils.isEmpty(friends)) {
            List<Channel> channels = friends.stream().map(CLIENT_MAP::get).collect(Collectors.toList());
            // 发送好友在线状态
            SetFriendStatusMessage message = new SetFriendStatusMessage();
            message.setId(userId);
            message.setStatus(status);
            BaseMessageData<SetFriendStatusMessage> baseMessageData = new BaseMessageData<>();
            baseMessageData.setData(message);
            baseMessageData.setType(IMP.SET_FRIEND_STATUS.getName());
            channelGroup.writeAndFlush(new TextWebSocketFrame(baseMessageData.toString()), channels::contains);

            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setId(userId);
            systemMessage.setSystem(true);
            systemMessage.setType(IMP.FRIEND.getName());
            systemMessage.setContent(Objects.equals(status, ONLINE) ? "对方已上线" : "对方已掉线");
            BaseMessageData<SystemMessage> systemBaseMessageData = new BaseMessageData<>();
            systemBaseMessageData.setType(IMP.SYSTEM.getName());
            systemBaseMessageData.setData(systemMessage);
            channelGroup.writeAndFlush(new TextWebSocketFrame(systemBaseMessageData.toString()), channels::contains);
        }
    }

    /**
     * 获取用户ID
     *
     * @param token
     * @return
     * @throws TException
     */
    private long getUserIdByToken(String token) throws TException {
        // 获取用户ID
        UserThriftService.Client userService = UserServerProvider.getUserService();
        return userService.getUserIdByToken(token);
    }

    /**
     * 转发消息给其他客户端
     *
     * @param text
     * @param client
     */
    private void sendChatMessage(String text, Channel client) {
        DownstreamMessageData messageData = JsonUtil.fromJson(text, DownstreamMessageData.class);
        DownstreamMessageData.DataBean data = messageData.getData();
        DownstreamMessageData.DataBean.MineBean mine = data.getMine();

        // 发送给客户端消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUsername(mine.getUsername());
        chatMessage.setAvatar(mine.getAvatar());
        chatMessage.setId(mine.getId());
        chatMessage.setType(data.getTo().getType());
        chatMessage.setContent(mine.getContent());
        chatMessage.setMine(false);
        chatMessage.setFromid(mine.getId());
        chatMessage.setTimestamp(System.currentTimeMillis());

        BaseMessageData<ChatMessage> baseMessageData = new BaseMessageData<>();
        baseMessageData.setData(chatMessage);
        baseMessageData.setType(IMP.CHAT.getName());
        Channel toChannel = CLIENT_MAP.get(Long.parseLong(data.getTo().getId()));
        channelGroup.writeAndFlush(new TextWebSocketFrame(baseMessageData.toString()),
                channel -> channel == toChannel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            // 断开连接发送消息
            log.info("客户端断开连接：{}", ctx.channel().id().asLongText());
            channelGroup.remove(ctx.channel());
            Long removeId = WsServer.getInstance().getUserId(ctx.channel());
            if (removeId != null) {
                CLIENT_MAP.remove(removeId);
                // 将用户置为离线状态
                setUserStatus(removeId, OFFLINE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        log.info("WebSocket Client:" + channel.id().asLongText() + "异常");
        cause.printStackTrace();
        ctx.close();
    }

}
