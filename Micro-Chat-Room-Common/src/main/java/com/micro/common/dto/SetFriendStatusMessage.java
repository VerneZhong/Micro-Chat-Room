package com.micro.common.dto;

import com.micro.common.util.JsonUtil;
import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-07-02 10:42
 */
@Data
public class SetFriendStatusMessage {
    private Long id;
    private String status;

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
