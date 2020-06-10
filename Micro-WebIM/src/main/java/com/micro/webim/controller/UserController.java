package com.micro.webim.controller;

import com.micro.common.code.BusinessCode;
import com.micro.common.dto.UserDTO;
import com.micro.common.response.ResultVO;
import com.micro.common.util.MD5Util;
import com.micro.webim.configuration.RedisClient;
import com.micro.webim.entity.User;
import com.micro.webim.request.UserLoginReq;
import com.micro.webim.request.UserRegisterReq;
import com.micro.webim.resp.GetListResp;
import com.micro.webim.resp.GetMembersResp;
import com.micro.webim.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户 ctrl
 *
 * @author Mr.zxb
 * @date 2020-06-10 09:22
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisClient redisClient;

    @GetMapping("/getList.json")
    public ResultVO<GetListResp> getList(@RequestParam Long userId) {
        log.info("获取用户列表 req: {}", userId);
        GetListResp resp = userService.getList(userId);
        return ResultVO.success(resp);
    }

    @GetMapping
    public ResultVO<GetMembersResp> getMembers(@RequestParam Long groupId) {
        log.info("获取群员列表 req: {}", groupId);
        GetMembersResp members = userService.getMembers(groupId);
        return ResultVO.success(members);
    }

    @PostMapping("/register.json")
    public ResultVO register(@RequestBody UserRegisterReq userRegisterReq) {
        log.info("注册新用户: {}", userRegisterReq.getNickname());
        userService.register(userRegisterReq);
        return ResultVO.success();
    }

    @GetMapping("/accountExists.json")
    public ResultVO accountExists(@RequestParam String account) {
        log.info("查看账户是否存在：{}", account);
        boolean exists = userService.accountExists(account);
        return ResultVO.success(exists);
    }

    @GetMapping("/login.json")
    public String login() {
        return "login";
    }

    @PostMapping("/login.json")
    public ResultVO login(@RequestBody UserLoginReq req) {
        log.info("注册新用户: {}", req.getAccount());
        User user = userService.login(req.getAccount(), MD5Util.md5(req.getPassword()));
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setAccount(user.getAccount());
            userDTO.setNickname(user.getNickname());
            userDTO.setAge(user.getAge());
            userDTO.setAvatar(user.getAvatarAddress());
            userDTO.setSign(user.getSign());
            userDTO.setAddress(user.getArea());

            redisClient.set(user.getId().toString(), userDTO);
            return ResultVO.success(userDTO);
        }
        return ResultVO.fail(BusinessCode.USER_INVALID);
    }
}
