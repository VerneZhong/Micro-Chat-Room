package com.micro.im.ws;

import com.micro.im.ws.handler.WebSocketServerHandler;
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
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     * 记录 chanel - userId 关联关系
     */
    public final static Map<Long, Channel> CLIENT_MAP = new ConcurrentHashMap<>();

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
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                            pipeline.addLast(new WebSocketServerHandler(channelGroup));
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
}
