package com.micro.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * 处理 Http 请求
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String webRoot = "webroot/";

    private File getResource(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(webRoot + fileName);
        log.info("resource path: {}", resource.getPath());
        return resource.getFile().exists() ? resource.getFile() : null;
    }

    private ByteBuf getContent(String fileName) {
        ClassPathResource resource = new ClassPathResource(webRoot + fileName);
        log.info("resource path: {}", resource.getPath());
        byte[] bytes = new byte[0];
        try {
            bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Unpooled.copiedBuffer(bytes);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();
        try {
            String page = uri.equals("/") ? "chat.html" : uri;
            RandomAccessFile file = new RandomAccessFile(getResource(page), "r");
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            String contextType = "text/html;";
            if (uri.endsWith(".css")) {
                contextType = "text/css;";
            } else if (uri.endsWith(".js")) {
                contextType = "text/javascript;";
            } else if (uri.toLowerCase().matches(".*\\.(jpg|png|gif)$")) {
                String ext = uri.substring(uri.lastIndexOf("."));
                contextType = "image/" + ext;
            }
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contextType + "charset=utf-8;");

            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));

            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.fireChannelRead(request.retain());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("Client:" + channel.remoteAddress() + "异常");
        // 出现异常关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}
