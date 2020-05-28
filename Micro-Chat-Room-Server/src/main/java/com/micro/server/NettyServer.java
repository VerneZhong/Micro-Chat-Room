package com.micro.server;

import com.micro.common.codec.IMDecoder;
import com.micro.common.codec.IMEncoder;
import com.micro.server.handler.HttpServerHandler;
import com.micro.server.handler.TerminalServerHandler;
import com.micro.server.handler.WebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import static com.micro.common.constant.ServerConstant.*;

/**
 * Netty Server
 *
 * @author Mr.zxb
 * @date 2020-05-20
 **/
@Slf4j
public final class NettyServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;

    private static NettyServer INSTANCE;

    private NettyServer() {
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new EpollEventLoopGroup();
        } else {
            bossGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new NioEventLoopGroup();
        }
    }

    public static NettyServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NettyServer();
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

                            // 添加 Handler 以及 编解码器
                            // 处理自定义协议
                            pipeline.addLast(new IMDecoder());
                            pipeline.addLast(new IMEncoder());
                            pipeline.addLast(new TerminalServerHandler());

                            // 处理 Http 请求
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 处理大文件数据流
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpServerHandler());

                            // 处理 WebSocket 请求
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast(new WebSocketServerHandler());
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
