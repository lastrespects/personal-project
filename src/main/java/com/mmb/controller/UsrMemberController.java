package com.mmb.controller;

import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/usr/member")
@RequiredArgsConstructor
public class UsrMemberController {

    private final MemberService memberService;

    // 회원가입 페이지 이동
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    // 회원가입 처리 (동기 방식)
    @PostMapping("/doJoin")
    public String doJoin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String nickname,
            @RequestParam int age,
            @RequestParam String region,
            @RequestParam int dailyTarget
    ) {
        memberService.join(username, password, name, nickname, age, region, dailyTarget);
        return "redirect:/login?msg=Welcome"; // 가입 후 로그인 페이지로 이동
    }

    // 아이디 중복 체크 (비동기 AJAX)
    @GetMapping("/checkUsername")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = memberService.isUsernameTaken(username);
        response.put("result", exists ? "fail" : "success");
        return response;
    }

    // 닉네임 중복 체크 (비동기 AJAX)
    @GetMapping("/checkNickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = memberService.isNicknameTaken(nickname);
        response.put("result", exists ? "fail" : "success");
        return response;
    }
}