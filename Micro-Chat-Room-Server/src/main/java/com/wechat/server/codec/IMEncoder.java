package com.wechat.server.codec;

import com.wechat.server.constant.ServerConstant;
import com.wechat.server.dto.IMMessage;
import com.wechat.server.protocol.IMP;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;
import org.springframework.util.StringUtils;

import static com.wechat.server.constant.ServerConstant.LEFT_BRACKET;
import static com.wechat.server.constant.ServerConstant.RIGHT_BRACKET;

/**
 * 自定义IM协议消息编码器
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out) throws Exception {
        out.writeBytes(new MessagePack().write(msg));
    }

    public String encode(IMMessage message) {
        if (null == message) {
            return "";
        }
        String cmd = message.getCmd();

        StringBuilder preface = new StringBuilder();
        preface.append(LEFT_BRACKET).append(cmd).append(RIGHT_BRACKET).append(LEFT_BRACKET).append(message.getTime()).append(RIGHT_BRACKET);

        if (IMP.LOGIN.getName().equals(cmd) || IMP.FLOWER.getName().equals(cmd)) {
            preface.append(LEFT_BRACKET).append(message.getSender()).append(RIGHT_BRACKET).append(LEFT_BRACKET).append(message.getTerminal()).append(RIGHT_BRACKET);
        } else if (IMP.CHAT.getName().equals(cmd)) {
            preface.append(LEFT_BRACKET).append(message.getSender()).append(RIGHT_BRACKET);
        } else if (IMP.SYSTEM.getName().equals(cmd)) {
            preface.append(LEFT_BRACKET).append(message.getOnline()).append(RIGHT_BRACKET);
        }

        if (!StringUtils.isEmpty(message.getContent())) {
            preface.append(" - ").append(message.getContent());
        }
        return preface.toString();
    }
}
