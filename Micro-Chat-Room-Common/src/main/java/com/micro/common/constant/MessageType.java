package com.micro.common.constant;

/**
 * Message Type
 *
 * @author Mr.zxb
 * @date 2020-06-17 20:50:20
 */
public enum MessageType {
    /**
     * 消息类型：
     * 1为请求添加用户消息；
     * 2为系统消息（同意添加好友）；
     * 3为系统消息（拒绝添加好友）；
     * 4为请求加群消息；
     * 5为系统消息（同意添加群系统消息）；
     * 6为系统消息（拒绝添加群系统消息）；
     * 7为全体用户消息（公告等）
     */
    USER_ADD_FRIEND(1, "申请添加你为好友"),
    SYSTEM_NOTICE_AGREE__ADD_FRIEND(2, "接受了你的添加请求并添加你为好友"),
    SYSTEM_NOTICE_REJECT_ADD_FRIEND(3, "拒绝了你的添加请求"),
    USER_ADD_GROUP(4, "申请进群"),
    SYSTEM_NOTICE_AGREE_ADD_GROUP(5, "已通过你的加群请求"),
    SYSTEM_NOTICE_REJECT_ADD_GROUP(6, "已拒绝你的加群请求"),
    ALL_USER_MESSAGE(7, "全体通知");

    private int type;
    private String message;

    MessageType(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public static String getMessage(int type) {
        switch (type) {
            case 1:
                return USER_ADD_FRIEND.message;
            case 2:
                return SYSTEM_NOTICE_AGREE__ADD_FRIEND.message;
            case 3:
                return SYSTEM_NOTICE_REJECT_ADD_FRIEND.message;
            case 4:
                return USER_ADD_GROUP.message;
            case 5:
                return SYSTEM_NOTICE_AGREE_ADD_GROUP.message;
            case 6:
                return SYSTEM_NOTICE_REJECT_ADD_GROUP.message;
            case 7:
                return ALL_USER_MESSAGE.message;
            default:
                return "其他消息";
        }
    }

    public int getType() {
        return type;
    }
}
