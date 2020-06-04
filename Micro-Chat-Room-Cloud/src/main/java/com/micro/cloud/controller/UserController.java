package com.micro.cloud.controller;

import com.micro.cloud.entity.User;
import com.micro.cloud.request.UserLoginReq;
import com.micro.cloud.service.UserService;
import com.micro.common.code.BusinessCode;
import com.micro.common.dto.UserDTO;
import com.micro.common.response.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

/**
 * 用户 ctrl
 * @author Mr.zxb
 * @date 2020-06-04 21:38:57
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    public ResultVO<UserDTO> login(@RequestBody @Valid UserLoginReq userLoginReq) {
        log.info("user login info: {}", userLoginReq);
        Optional<User> userOptional = userService.login(userLoginReq.getAccount(), userLoginReq.getPassword());
       return null;
    }
}
