package com.micro.client;

import com.micro.client.handler.ClientHandler;
import com.micro.common.codec.IMDecoder;
import com.micro.common.codec.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import static com.micro.common.constant.ServerConstant.SERVER_HOST;
import static com.micro.common.constant.ServerConstant.SERVER_PORT;

/**
 * Client Server
 * @author Mr.zxb
 * @date 2020-05-26
 **/
@Slf4j
public class NettyClient {

    private final EventLoopGroup workGroup;

    private static NettyClient INSTANCE;

    private String nickName;

    private NettyClient(String nickName) {
        if (Epoll.isAvailable()) {
            workGroup = new EpollEventLoopGroup();
        } else {
            workGroup = new NioEventLoopGroup();
        }
        this.nickName = nickName;
    }

    public static NettyClient getInstance(String nickName) {
        if (INSTANCE == null) {
            INSTANCE = new NettyClient(nickName);
        }
        return INSTANCE;
    }

    public void start() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IMDecoder());
                            pipeline.addLast(new IMEncoder());
                            pipeline.addLast(new ClientHandler(nickName));
                        }
                    });
            bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
            log.info("客户端已启动，已连接到服务端：{}:{}", SERVER_HOST, SERVER_PORT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        System.out.println("Netty Server destroy...");
        workGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        new NettyClient("zxb").start();
    }
}
