package com.wechat.cloud.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCellphoneNumber() {
        return cellphoneNumber;
    }

    public void setCellphoneNumber(String cellphoneNumber) {
        this.cellphoneNumber = cellphoneNumber;
    }

    public String getAvatarAddress() {
        return avatarAddress;
    }

    public void setAvatarAddress(String avatarAddress) {
        this.avatarAddress = avatarAddress;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Integer isLocked) {
        this.isLocked = isLocked;
    }

    public LocalDate getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }

    @Override
    protected Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "User{" +
        ", id=" + id +
        ", account=" + account +
        ", password=" + password +
        ", nickname=" + nickname +
        ", cellphoneNumber=" + cellphoneNumber +
        ", avatarAddress=" + avatarAddress +
        ", age=" + age +
        ", area=" + area +
        ", isLocked=" + isLocked +
        ", registerDate=" + registerDate +
        "}";
    }
}
