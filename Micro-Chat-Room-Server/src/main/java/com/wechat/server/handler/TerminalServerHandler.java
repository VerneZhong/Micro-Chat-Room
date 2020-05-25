package com.wechat.server.handler;

import com.wechat.server.dto.IMMessage;
import com.wechat.server.processor.MsgProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * IM协议处理
 * @author Mr.zxb
 * @date 2020-05-25
 **/
@Slf4j
public class TerminalServerHandler extends SimpleChannelInboundHandler<IMMessage> {

    private MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        processor.sendMessage(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("Socket Client: 与客户端断开连接：" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
