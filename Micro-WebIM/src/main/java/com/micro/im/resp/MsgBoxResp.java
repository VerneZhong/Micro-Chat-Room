package com.micro.im.resp;

import com.micro.im.vo.Mine;
import lombok.Data;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-17 15:04
 */
@Data
public class MsgBoxResp {
    private Long messageId;
    private Long from;
    private Mine user;
    private Integer type;
    private String friendGroupId;
    private String content;
    private String remark;
    private String time;
}
