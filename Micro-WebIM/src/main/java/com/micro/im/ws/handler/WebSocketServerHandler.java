package com.micro.im.ws.handler;

import com.micro.common.dto.ChatMessage;
import com.micro.common.util.JsonUtil;
import com.micro.im.ws.provider.UserServerProvider;
import com.micro.im.ws.receive.MessageData;
import com.micro.thrift.user.UserThriftService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;

import static com.micro.im.ws.WsServer.CLIENT_MAP;

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

    public WebSocketServerHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 和服务器建立连接
        channelGroup.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        log.info("ws message: {}", text);
        String type = JsonUtil.getRootValueByKey(text, "type");
        if (type != null) {
            switch (type) {
                case "login":
                    // 发送上线消息
                    String uid = JsonUtil.getRootValueByKey(text, "uid");
                    if (uid != null) {
                        CLIENT_MAP.put(Long.parseLong(uid), ctx.channel());
                    }
                    break;
                case "chatMessage":
                    sendChatMessage(text);
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
     * 转发消息给其他客户端
     * @param text
     */
    private void sendChatMessage(String text) {
        MessageData messageData = JsonUtil.fromJson(text, MessageData.class);
        MessageData.DataBean data = messageData.getData();
        ChatMessage chatMessage = new ChatMessage();
        MessageData.DataBean.MineBean mine = data.getMine();
        chatMessage.setUsername(mine.getUsername());
        chatMessage.setAvatar(mine.getAvatar());
        chatMessage.setId(mine.getId());
        chatMessage.setType(data.getTo().getType());
        chatMessage.setContent(mine.getContent());
        chatMessage.setMine(false);
        chatMessage.setFromid(mine.getId());
        chatMessage.setTimestamp(System.currentTimeMillis());

        Channel toChannel = CLIENT_MAP.get(Long.parseLong(data.getTo().getId()));
        channelGroup.writeAndFlush(new TextWebSocketFrame(chatMessage.toString()),
                channel -> channel == toChannel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开连接发送消息
        log.info("客户端断开连接：{}", ctx.channel().id().asLongText());
        channelGroup.remove(ctx.channel());
        Long removeId = null;
        for (Map.Entry<Long, Channel> channelEntry : CLIENT_MAP.entrySet()) {
            if (channelEntry.getValue() == ctx.channel()) {
                removeId = channelEntry.getKey();
                break;
            }
        }
        if (removeId != null) {
            CLIENT_MAP.remove(removeId);
            // 将用户置为离线状态
            log.info("set user online status: {}", removeId);
            UserThriftService.Client userService = UserServerProvider.getUserService();
            userService.setUserOffline(removeId, "offline");
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
