package com.micro.im.ws.handler;

import com.micro.common.protocol.IMP;
import com.micro.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * ws handler
 *
 * @author Mr.zxb
 * @date 2020-06-12 16:52
 */
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 发送上线消息

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        log.info("ws message: {}", text);
        String type = JsonUtil.getRootValueByKey(text, "type");
        if (type != null) {
            switch (IMP.LOGIN.getName()) {
                case "login" :
                    String token = JsonUtil.getRootValueByKey(text, "token");
                    break;
                case "chat" :

                    break;
                case "system" :

                    break;
                case "logout" :

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 断开连接发送消息

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        log.info("WebSocket Client:" + channel.id().asLongText() + "异常");
        cause.printStackTrace();
        ctx.close();
    }
}
