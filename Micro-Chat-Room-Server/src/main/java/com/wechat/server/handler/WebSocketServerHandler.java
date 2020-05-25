package com.wechat.server.handler;

import com.wechat.server.processor.MsgProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 协议处理
 * @author Mr.zxb
 * @date 2020-05-25
 **/
@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        processor.sendMessage(ctx.channel(), msg.text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        String address = processor.getAddress(channel);
        log.info("WebSocket Client:" + address + "异常");
        cause.printStackTrace();
        ctx.close();
    }
}
