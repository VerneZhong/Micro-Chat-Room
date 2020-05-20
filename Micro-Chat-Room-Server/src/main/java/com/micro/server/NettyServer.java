package com.micro.server;

import com.micro.server.constant.ServerConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

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

    public NettyServer() {
        if (Epoll.isAvailable()) {
            this.bossGroup = new EpollEventLoopGroup(1);
            this.workGroup = new EpollEventLoopGroup();
        } else {
            this.bossGroup = new NioEventLoopGroup(1);
            this.workGroup = new NioEventLoopGroup();
        }
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
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
        channel.close();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
