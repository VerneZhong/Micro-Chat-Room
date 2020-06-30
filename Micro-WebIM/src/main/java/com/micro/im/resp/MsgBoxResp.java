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
    private Long msgIdx;
    private Long from;
    private Mine fromInfo;
    private Long to;
    private Mine toInfo;
    private Integer msgType;
    private String friendGroupId;
    private String content;
    private String remark;
    private String sendTime;
    private String readTime;
    private Integer status;
    private String handle;
}
