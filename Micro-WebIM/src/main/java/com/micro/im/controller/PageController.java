package com.micro.im.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
