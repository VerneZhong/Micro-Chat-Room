package com.wechat.server;

import com.wechat.server.constant.ServerConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import static com.wechat.server.constant.ServerConstant.DEFAULT_EVENT_LOOP_THREAD;

/**
 * Netty Server
 *
 * @author Mr.zxb
 * @date 2020-05-20
 **/
public class NettyServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;
    private Channel channel;

    private static NettyServer INSTANCE;

    private NettyServer() {
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new EpollEventLoopGroup();
        } else if (KQueue.isAvailable()) {
            bossGroup = new KQueueEventLoopGroup(DEFAULT_EVENT_LOOP_THREAD);
            workGroup = new KQueueEventLoopGroup();
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
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : KQueue.isAvailable() ? KQueueServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 添加 Handler 以及 编解码器
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addFirst(new IdleStateHandler(0, 0, 60));
                        }
                    })
                    .localAddress(ServerConstant.SERVER_PORT);
            ChannelFuture future = bootstrap.bind().sync();
            System.out.println("Netty Server Running...");
            this.channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        System.out.println("Netty Server destroy...");
        channel.close();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
