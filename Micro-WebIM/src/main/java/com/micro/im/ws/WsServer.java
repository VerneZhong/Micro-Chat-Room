package com.micro.im.ws;

import com.google.common.collect.Maps;
import com.micro.im.ws.handler.WebSocketServerHandler;
import com.micro.im.ws.upstream.BaseMessageData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;


import java.util.Map;

import static com.micro.common.constant.ServerConstant.*;

/**
 * Ws server
 *
 * @author Mr.zxb
 * @date 2020-06-12 16:20
 */
@Slf4j
public class WsServer {

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workGroup;

    private static WsServer INSTANCE;

    /**
     * 记录在线人数
     */
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * Map: {userId, Channel}
     */
    public static final Map<Long, Channel> CLIENT_MAP = Maps.newConcurrentMap();

    /**
     * 绑定 channel --> userId 之间参数
     */
    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    private WsServer() throws Exception {
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new EpollEventLoopGroup();
        } else {
            bossGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new NioEventLoopGroup();
        }
    }

    public static WsServer getInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new WsServer();
        }
        return INSTANCE;
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加 Ssl
//                            SSLEngine sslEngine = createSSLContext().createSSLEngine();
//                            sslEngine.setUseClientMode(false);
//                            sslEngine.setNeedClientAuth(true);
//                            pipeline.addFirst(new SslHandler(sslEngine));

                            // 添加 Handler 以及 编解码器
                            // 处理自定义协议
//                            pipeline.addLast(new IMDecoder());
//                            pipeline.addLast(new IMEncoder());

                            // 处理 Http 请求
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 处理大文件数据流
                            pipeline.addLast(new ChunkedWriteHandler());

                            // 处理 WebSocket 请求
                            pipeline.addLast(new WebSocketServerCompressionHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im", null, true));
                            pipeline.addLast(new WebSocketServerHandler(channelGroup, CLIENT_MAP));
                        }
                    })
                    .bind(SERVER_PORT).sync();
            log.info("服务已启动，监听端口:{}，访问地址：{}", SERVER_PORT, SERVER_HOST + ":" + SERVER_PORT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        log.info("服务已关闭");
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    /**
     * 获取当前 client 的 userId
     * @param client
     * @return
     */
    public Long getUserId(Channel client) {
        return client.attr(USER_ID_KEY).get();
    }

    /**
     * 指定channel发送消息
     * @param channel
     * @param messageData
     * @param <T>
     */
    public  <T> void sendMessage(Channel channel, BaseMessageData<T> messageData) {
        log.info("send ws client:{}, message:{}", channel, messageData);
        channelGroup.writeAndFlush(new TextWebSocketFrame(messageData.toString()), target -> channel == target);
    }

    /**
     * 发送消息
     * @param messageData
     * @param <T>
     */
    public  <T> void sendMessage(BaseMessageData<T> messageData) {
        channelGroup.writeAndFlush(new TextWebSocketFrame(messageData.toString()));
    }
}
