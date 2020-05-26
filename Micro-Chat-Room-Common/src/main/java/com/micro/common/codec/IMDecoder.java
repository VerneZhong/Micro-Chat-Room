package com.micro.common.codec;

import cn.minsin.core.tools.StringUtil;
import com.micro.common.dto.IMMessage;
import com.micro.common.protocol.IMP;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.micro.common.constant.ServerConstant.LEFT_BRACKET;
import static com.micro.common.constant.ServerConstant.RIGHT_BRACKET;


/**
 * 自定义IM协议消息解码器
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
public class IMDecoder extends ByteToMessageDecoder {

    /**
     * IM 请求内容的正则
     */
    private Pattern pattern = Pattern.compile("^\\[(.*)](\\s-\\s(.*))?");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 获取可读字节数
        int length = in.readableBytes();
        byte[] bytes = new byte[length];
        String content = new String(bytes, in.readerIndex(), length);

        // 空消息不解析
        if (StringUtils.isEmpty(content)) {
            if (!IMP.isIMP(content)) {
                ctx.channel().pipeline().remove(this);
            }
        }

        try {
            in.getBytes(in.readerIndex(), bytes, 0, length);
            out.add(new MessagePack().read(bytes, IMMessage.class));
            in.clear();
        } catch (MessageTypeException e) {
            ctx.channel().pipeline().remove(this);
        }
    }

    /**
     * 字符串解析成IM消息
     * @param msg
     * @return
     */
    public IMMessage decode(String msg) {
        if (StringUtil.isEmpty(msg)) {
            return null;
        }
        try {
            Matcher m = pattern.matcher(msg);

            String header = "";
            String content = "";
            if (m.matches()) {
                header = m.group(1);
                content = m.group(3);
            }

            String[] headers = header.split("\\]\\[");
            long time = Long.parseLong(headers[1]);
            String nickName = headers[2];

            nickName = nickName.length() < 10 ? nickName : nickName.substring(0, 9);

            if (msg.startsWith(LEFT_BRACKET + IMP.LOGIN.getName() + RIGHT_BRACKET)) {
                return new IMMessage(headers[0], headers[3], time, nickName);
            } else if (msg.startsWith(LEFT_BRACKET + IMP.CHAT.getName() + RIGHT_BRACKET)) {
                return new IMMessage(headers[0], time, nickName, content);
            } else if (msg.startsWith(LEFT_BRACKET + IMP.FLOWER + RIGHT_BRACKET)) {
                return new IMMessage(headers[0], headers[3], time, nickName);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
