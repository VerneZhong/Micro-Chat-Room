package com.micro.im.resp;

import com.micro.common.dto.UserDTO;
import lombok.Data;

/**
 * @author Mr.zxb
 * @date 2020-06-14 11:38:44
 */
@Data
public class LoginResp {

    private String token;
    private UserDTO user;
}
