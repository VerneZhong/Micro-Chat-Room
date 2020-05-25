package com.wechat.server.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * @author Mr.zxb
 * @date 2020-05-20 20:35:09
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 发送心跳信息
            ctx.writeAndFlush(Unpooled.copiedBuffer("HeartBeat:" + ctx.channel(), CharsetUtil.UTF_8));
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
