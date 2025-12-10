package com.mmb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "usr/member/login";
    }

    @GetMapping("/usr/member/login")
    public String legacyLogin() {
        return "redirect:/login";
    }
}
