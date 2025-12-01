package com.mmb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "usr/member/login"; // 경로 확인 필요 (src/main/webapp/view/usr/member/login.jsp)
    }
}