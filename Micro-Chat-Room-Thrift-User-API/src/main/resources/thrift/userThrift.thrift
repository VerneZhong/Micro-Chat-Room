namespace java com.micro.thrift.user

struct UserInfo {

    i64 id,

    /**
     * 用户名
     */
    string account,

    /**
    * 昵称
    */
    string nickname,

    /**
     * 密码
     */
    string password,

    /**
     * 年龄
     */
    i32 age,

    /**
     * 头像
     */
    string avatar,

    /**
     * 邀请码
     */
    string invitationCode,

    /**
     * 地址
     */
    string address
}

service UserService {
    UserInfo getUserById(1:i32 id);

    UserInfo login(1:string account, 2:string password);

    void registerUser(1:UserInfo user);

    bool isLock(1:string account);

    bool accountExists(1:string account);
}