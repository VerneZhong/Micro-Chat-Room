package com.micro.im.controller;

import com.micro.common.dto.UserDTO;
import com.micro.im.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mr.zxb
 * @date 2020-06-13 18:12:25
 */
@Controller
public class PageController {
    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String index(HttpServletRequest request) {
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

    @GetMapping("/find")
    public String find() {
        return "find";
    }

    @GetMapping("/chatlog")
    public String chatlog() {
        return "chatlog";
    }

    @GetMapping("/msgbox")
    public String msgbox() {
        return "msgbox";
    }
}
