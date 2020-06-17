namespace java com.micro.thrift.user

service UserThriftService {
    void setUserOffline(1:i64 userId, 2:string status);
}