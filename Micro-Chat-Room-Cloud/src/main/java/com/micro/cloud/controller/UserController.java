package com.micro.cloud.controller;

import com.micro.cloud.resp.GetListResp;
import com.micro.cloud.resp.GetMembersResp;
import com.micro.cloud.service.UserService;
import com.micro.common.response.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
