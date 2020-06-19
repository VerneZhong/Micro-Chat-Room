package com.micro.common.constant;

/**
 * 静态常量
 *
 * @author Mr.zxb
 * @date 2020-05-20
 **/
public final class ServerConstant {

    /**
     * Netty Server Port
     *
     */
    public static final int SERVER_PORT = 8080;

    /**
     * localhost
     */
    public static final String SERVER_HOST = "127.0.0.1";

    /**
     * thrift rpc server port
     */
    public static final int THRIFT_USER_PORT = 8086;

    /**
     * Default Boss EventLoopGroup Thread Number
     */
    public static final int DEFAULT_EVENT_LOOP_THREAD = 1;

    public static final String LEFT_BRACKET = "[";

    public static final String RIGHT_BRACKET = "]";

    public static final String CONSOLE = "Console";

    public static final String EXIT = "EXIT";

    public static final String BASE_UPLOAD = "/upload/";

    public static final String UPLOAD_IMG_PATH = "/upload/img/";

    public static final String UPLOAD_VIDEO_PATH = "/upload/video/";

    public static final String UPLOAD_FILE_PATH = "/upload/file/";

    public static final String ONLINE = "online";

    public static final String OFFLINE = "offline";
}
