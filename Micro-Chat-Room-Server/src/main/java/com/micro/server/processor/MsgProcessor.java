package com.micro.server.processor;

import com.alibaba.fastjson.JSONObject;
import com.micro.common.codec.IMDecoder;
import com.micro.common.codec.IMEncoder;
import com.micro.common.dto.IMMessage;
import com.micro.common.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import static com.micro.common.constant.ServerConstant.CONSOLE;

/**
 * 自定义协议内容的逻辑处理
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
public class MsgProcessor {

    /**
     * 在线用户
     */
    public static final ChannelGroup ONLINE_USERS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 扩展属性
     */
    public static final AttributeKey<String> NICE_NAME = AttributeKey.valueOf("nickName");
    public static final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    public static final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attr");
    public static final AttributeKey<String> FROM = AttributeKey.valueOf("from");

    /**
     * 消息编解码器
     */
    private IMDecoder decoder = new IMDecoder();
    private IMEncoder encoder = new IMEncoder();

    /**
     * 获取用户昵称
     *
     * @param client
     * @return
     */
    public String getNickName(Channel client) {
        return client.attr(NICE_NAME).get();
    }

    /**
     * 获取用户远程IP地址
     *
     * @param client
     * @return
     */
    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    /**
     * 获取扩展属性
     *
     * @param client
     * @return
     */
    public JSONObject getAttrs(Channel client) {
        try {
            return client.attr(ATTRS).get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置扩展属性
     *
     * @param client
     * @param key
     * @param val
     */
    public void setAttrs(Channel client, String key, Object val) {
        try {
            JSONObject json = client.attr(ATTRS).get();
            json.put(key, val);
            client.attr(ATTRS).set(json);
        } catch (Exception e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, val);
            client.attr(ATTRS).set(jsonObject);
        }
    }

    /**
     * 登出通知
     *
     * @param client
     */
    public void logout(Channel client) {
        if (getNickName(client) == null) {
            return;
        }

        // 通知其他在线用户，有用户已离开
        for (Channel onlineUser : ONLINE_USERS) {
            IMMessage request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), ONLINE_USERS.size(), getNickName(client) + "离开");
            onlineUser.writeAndFlush(new TextWebSocketFrame(encoder.encode(request)));
        }
        ONLINE_USERS.remove(client);
    }

    /**
     * 发送消息
     *
     * @param client
     * @param message
     */
    public void sendMessage(Channel client, IMMessage message) {
        sendMessage(client, encoder.encode(message));
    }

    /**
     * 发送消息
     *
     * @param client
     * @param message
     */
    public void sendMessage(Channel client, String message) {
        IMMessage request = decoder.decode(message);
        if (null == request) {
            return;
        }
        if (request.getCmd().equals(IMP.LOGIN.getName())) {
            withLogin(client, request);
        } else if (request.getCmd().equals(IMP.CHAT.getName())) {
            withChat(client, request);
        } else if (request.getCmd().equals(IMP.FLOWER.getName())) {
            withFlower(client, request);
        }
    }

    /**
     * 发送送花消息
     * @param client
     * @param im
     */
    private void withFlower(Channel client, IMMessage im) {
        JSONObject attrs = getAttrs(client);
        if (null != attrs) {
            long lastTime = attrs.getLongValue("lastFlowerTime");

            // 60s内不允许重复刷鲜花
            int seconds = 10;
            long sub = sysTime() - lastTime;
            if (sub < 1000 * seconds) {
                im.setSender("you");
                im.setCmd(IMP.SYSTEM.getName());
                im.setContent(String.format("您送鲜花太频繁,%d秒后再试", (seconds - Math.round(sub / 1000))));

                client.writeAndFlush(new TextWebSocketFrame(encoder.encode(im)));
                return;
            }
        }

        // 正常送花
        for (Channel channel : ONLINE_USERS) {
            if (channel == client) {
                im.setSender("you");
                im.setContent("你给大家送了一波鲜花雨");
                setAttrs(client, "lastFlowerTime", sysTime());
            } else {
                im.setSender(getNickName(client));
                im.setContent(getNickName(client) + "送来一波鲜花雨");
            }
            im.setTime(sysTime());

            channel.writeAndFlush(new TextWebSocketFrame(encoder.encode(im)));
        }
    }

    /**
     * 发送聊天消息
     * @param client
     * @param im
     */
    private void withChat(Channel client, IMMessage im) {
        for (Channel channel : ONLINE_USERS) {
            boolean isSelf = channel == client;
            if (isSelf) {
                im.setSender("you");
            } else {
                im.setSender(getNickName(client));
            }
            im.setTime(sysTime());

            if (CONSOLE.equals(channel.attr(FROM).get()) && !isSelf) {
                channel.writeAndFlush(im);
                continue;
            }
            channel.writeAndFlush(new TextWebSocketFrame(encoder.encode(im)));
        }
    }

    /**
     * 发送登陆消息
     * @param client
     * @param im
     */
    private void withLogin(Channel client, IMMessage im) {
        client.attr(NICE_NAME).getAndSet(im.getSender());
        client.attr(IP_ADDR).getAndSet(getAddress(client));
        client.attr(FROM).getAndSet(im.getTerminal());
        ONLINE_USERS.add(client);

        for (Channel onlineUser : ONLINE_USERS) {
            boolean isSelf = onlineUser == client;
            if (!isSelf) {
                im = new IMMessage(IMP.SYSTEM.getName(), sysTime(), ONLINE_USERS.size(), getNickName(client) + "加入");
            } else {
                im = new IMMessage(IMP.SYSTEM.getName(), sysTime(), ONLINE_USERS.size(), "已与服务器建立连接");
            }

            if (CONSOLE.equals(onlineUser.attr(FROM).get())) {
                onlineUser.writeAndFlush(im);
            }
            onlineUser.writeAndFlush(new TextWebSocketFrame(encoder.encode(im)));
        }
    }

    private long sysTime() {
        return System.currentTimeMillis();
    }

}
