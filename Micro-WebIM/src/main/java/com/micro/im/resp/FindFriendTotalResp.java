package com.micro.im.resp;

import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-13 18:29:55
 */
@Data
public class FindFriendTotalResp {
    private Integer count;
    private Integer limit;

    public FindFriendTotalResp(Integer count) {
        this.count = count;
        this.limit = 10;
    }
}
