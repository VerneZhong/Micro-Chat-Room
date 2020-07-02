namespace java com.micro.thrift.user

service UserThriftService {
    void setUserOffline(1:i64 userId, 2:string status);

    i64 getUserIdByToken(1:string token);

    list<i64> getFriendByUserId(1:i64 userId);
}