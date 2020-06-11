package com.micro.im.controller;

import com.micro.common.code.BusinessCode;
import com.micro.common.dto.UserDTO;
import com.micro.common.response.ResultVO;
import com.micro.common.util.MD5Util;
import com.micro.common.util.TokenUtil;
import com.micro.im.configuration.RedisClient;
import com.micro.im.entity.User;
import com.micro.im.request.UserLoginReq;
import com.micro.im.request.UserRegisterReq;
import com.micro.im.resp.GetListResp;
import com.micro.im.resp.GetMembersResp;
import com.micro.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户 ctrl
 *
 * @author Mr.zxb
 * @date 2020-06-10 09:22
 */
@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisClient redisClient;

    @GetMapping("/getList.do")
    @ResponseBody
    public ResultVO<GetListResp> getList(@RequestParam String token) {
        log.info("获取用户列表 req: {}", token);
        if (StringUtils.isBlank(token)) {
            return ResultVO.fail(BusinessCode.PARAM_ERROR);
        }
        UserDTO userDTO = authentication(token);
        if (userDTO == null) {
            // 需要重新登录

        }
        GetListResp resp = userService.getList(userDTO.getId());
        return ResultVO.success(resp);
    }

    @GetMapping("/getMembers.do")
    @ResponseBody
    public ResultVO<GetMembersResp> getMembers(@RequestParam Long groupId) {
        log.info("获取群员列表 req: {}", groupId);
        GetMembersResp members = userService.getMembers(groupId);
        return ResultVO.success(members);
    }

    @PostMapping("/register.do")
    @ResponseBody
    public ResultVO register(@RequestBody UserRegisterReq userRegisterReq) {
        log.info("注册新用户: {}", userRegisterReq.getNickname());
        userService.register(userRegisterReq);
        return ResultVO.success();
    }

    @GetMapping("/accountExists.do")
    @ResponseBody
    public ResultVO accountExists(@RequestParam String account) {
        log.info("查看账户是否存在：{}", account);
        boolean exists = userService.accountExists(account);
        return ResultVO.success(exists);
    }

    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/login.do")
    @ResponseBody
    public ResultVO login(@RequestBody UserLoginReq req) {
        log.info("用户登录: {}", req.getAccount());
        User user = userService.login(req.getAccount(), req.getPassword());
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setAccount(user.getAccount());
            userDTO.setNickname(user.getNickname());
            userDTO.setAge(user.getAge());
            userDTO.setAvatar(user.getAvatarAddress());
            userDTO.setSign(user.getSign());
            userDTO.setAddress(user.getArea());

            // 生成token
            String token = TokenUtil.getToken();

            // 缓存用户到Redis
            redisClient.set(token, userDTO, 3600);
            redisClient.set(user.getId().toString(), "online");
            return ResultVO.success(token);
        }
        return ResultVO.fail(BusinessCode.USER_INVALID);
    }

    @PostMapping("/authentication")
    @ResponseBody
    public UserDTO authentication(@RequestHeader("token") String token) {
        return (UserDTO) redisClient.get(token);
    }


}
