package com.micro.client.handler;

import com.micro.common.constant.ServerConstant;
import com.micro.common.dto.IMMessage;
import com.micro.common.protocol.IMP;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.micro.common.constant.ServerConstant.CONSOLE;
import static com.micro.common.constant.ServerConstant.EXIT;

/**
 * Client Handler
 * @author Mr.zxb
 * @date 2020-05-26
 **/
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<IMMessage> {

    private ChannelHandlerContext ctx;
    private String nickName;

    public ClientHandler(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 与服务器成功建立连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        IMMessage imMessage = new IMMessage(IMP.LOGIN.getName(), ServerConstant.CONSOLE, System.currentTimeMillis(), nickName);
        sendMessage(imMessage);
        log.info("成功连接服务器，开始登陆");
        session();
    }

    /**
     * 发送消息
     * @param message
     * @return
     */
    private boolean sendMessage(IMMessage message) {
        ctx.channel().writeAndFlush(message);
        log.info("继续输入开始对话");
        return IMP.LOGOUT.getName().equals(message.getCmd());
    }

    /**
     * 客户端控制台对话
     */
    private void session() {
        ctx.executor().execute(() -> {
            log.info(nickName + ", 您好，请在控制台输入对话内容");
            IMMessage message = null;
            Scanner scanner = new Scanner(System.in);
            do {
                if (scanner.hasNext()) {
                    String input = scanner.nextLine();
                    if (EXIT.equals(input)) {
                        message = new IMMessage(IMP.LOGOUT.getName(), CONSOLE, System.currentTimeMillis(), nickName);
                    } else {
                        message = new IMMessage(IMP.CHAT.getName(), System.currentTimeMillis(), nickName, input);
                    }
                }
            } while (sendMessage(message));
            scanner.close();
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        log.info(null == msg.getSender() ? "" : msg.getSender() + ":" + removeHtmlTag(msg.getContent()));
    }

    public String removeHtmlTag(String htmlStr) {
        // 定义 script 的正则
        String reqEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        // 定义 css 的正则
        String reqEx_css = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        // 定义 html 标签的正则
        String reqEx_html = "<[^>]+>";

        Pattern p_script = Pattern.compile(reqEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        // 过滤 script 标签
        htmlStr = m_script.replaceAll("");

        Pattern p_css = Pattern.compile(reqEx_css, Pattern.CASE_INSENSITIVE);
        Matcher m_css = p_css.matcher(htmlStr);
        // 过滤 css 标签
        htmlStr = m_css.replaceAll("");

        Pattern p_html = Pattern.compile(reqEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        // 过滤 css 标签
        htmlStr = m_html.replaceAll("");

        // 返回文本字符串
        return htmlStr.trim();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("与服务器断开连接:" + cause.getMessage());
        ctx.close();
    }
}
