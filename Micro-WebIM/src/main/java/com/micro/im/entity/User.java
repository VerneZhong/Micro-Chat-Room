package com.micro.im.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
/**
 * <p>
 * 
 * </p>
 *
 * @author Mr.zxb
 * @since 2020-06-10
 */
@Data
public class User extends Model<User> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帐号名称
     */
    private String account;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String cellphoneNumber;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatarAddress;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 地区
     */
    private String area;

    /**
     * 0未锁定，1锁定
     */
    private Integer isLocked;

    /**
     * 注册日期
     */
    private LocalDate registerDate;

    /**
     * 签名
     */
    private String sign;

    @Override
    protected Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", cellphoneNumber='" + cellphoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", avatarAddress='" + avatarAddress + '\'' +
                ", age=" + age +
                ", area='" + area + '\'' +
                ", isLocked=" + isLocked +
                ", registerDate=" + registerDate +
                ", sign='" + sign + '\'' +
                "} " + super.toString();
    }
}
