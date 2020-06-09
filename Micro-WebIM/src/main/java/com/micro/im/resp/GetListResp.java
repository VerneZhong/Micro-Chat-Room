package com.micro.im.resp;

import com.micro.im.vo.FriendGroup;
import com.micro.im.vo.GroupVO;
import com.micro.im.vo.Mine;
import lombok.Data;

import java.util.List;

/**
 * @author Mr.zxb
 * @date 2020-06-09 20:57:51
 */
@Data
public class GetListResp {
    private Mine mine;
    private List<FriendGroup> friend;
    private List<GroupVO> group;
}
