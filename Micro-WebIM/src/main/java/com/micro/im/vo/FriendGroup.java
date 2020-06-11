package com.micro.im.vo;

import lombok.Data;

import java.util.List;

/**
 * 好友分组 vo
 * @author Mr.zxb
 * @date 2020-06-09 20:49:06
 */
@Data
public class FriendGroup {
    private String groupname;
    private Long id;
    private List<FriendVO> list;
}
