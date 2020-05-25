package com.wechat.cloud.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
/**
 * <p>
 * 
 * </p>
 *
 * @author Mr.zxb
 * @since 2020-05-20
 */
public class UserFriends extends Model<UserFriends> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 好友账号
     */
    private String friendsAccount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getFriendsAccount() {
        return friendsAccount;
    }

    public void setFriendsAccount(String friendsAccount) {
        this.friendsAccount = friendsAccount;
    }

    @Override
    protected Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "UserFriends{" +
        ", id=" + id +
        ", account=" + account +
        ", friendsAccount=" + friendsAccount +
        "}";
    }
}
