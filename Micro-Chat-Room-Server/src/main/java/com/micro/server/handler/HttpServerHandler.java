package com.micro.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * 处理 Http 请求
 *
 * @author Mr.zxb
 * @date 2020-05-25
 **/
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String webRoot = "webroot";

    private File getResource(String fileName) throws Exception {
        String path = webRoot + File.separator + fileName;
        path = path.replaceAll("//", "/");
        log.info("resource path: {}", path);
        ClassPathResource classPathResource = new ClassPathResource(path);
        InputStream inputStream = classPathResource.getInputStream();

        String property = System.getProperty("user.dir") + "/";
        File target = new File(property + path);
        FileUtils.copyInputStreamToFile(inputStream, target);
        return target;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        try {
            // Handle a bad request.
            if (!req.decoderResult().isSuccess()) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), BAD_REQUEST,
                        ctx.alloc().buffer(0)));
            }

            // Allow only Get methods.
            if (!HttpMethod.GET.equals(req.method())) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), FORBIDDEN,
                        ctx.alloc().buffer(0)));
            }

            String uri = req.uri();
            String page = "/".equals(uri) ? "chat.html" : uri;
            File resource = getResource(page);
            RandomAccessFile file = new RandomAccessFile(resource, "r");
            String contextType = "text/html;";
            if (uri.endsWith(".css")) {
                contextType = "text/css;";
            } else if (uri.endsWith(".js")) {
                contextType = "text/javascript;";
            } else if (uri.toLowerCase().matches(".*\\.(jpg|png|gif)$")) {
                String ext = uri.substring(uri.lastIndexOf(".") + 1);
                contextType = "image/" + ext + ";";
            }
            HttpResponse res = new DefaultHttpResponse(req.protocolVersion(), OK);

            res.headers().set(CONTENT_TYPE, contextType + "; charset=UTF-8");
            HttpUtil.setContentLength(res, file.length());

            boolean keepAlive = HttpUtil.isKeepAlive(req);

            if (keepAlive) {
                HttpUtil.setContentLength(res, file.length());
                res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(res);
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));

            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
            file.close();
        } catch (Exception e) {
            ctx.fireChannelRead(req.retain());
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not ok (200).
        HttpResponseStatus responseStatus = res.status();
        if (responseStatus.code() != 200) {
            ByteBufUtil.writeUtf8(res.content(), responseStatus.toString());
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary
        boolean keepAlive = HttpUtil.isKeepAlive(req) && responseStatus.code() == 200;
        HttpUtil.setKeepAlive(res, keepAlive);
        ChannelFuture future = ctx.writeAndFlush(res);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
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
