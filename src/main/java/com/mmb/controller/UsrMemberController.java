package com.mmb.controller;

import com.mmb.dto.ResultData;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/usr/member")
@RequiredArgsConstructor
public class UsrMemberController {

    private final MemberService memberService;

    // 회원가입 페이지
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    // 회원가입 처리
    @PostMapping("/doJoin")
    public String doJoin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestParam int age,
            @RequestParam String region,
            @RequestParam int dailyTarget
    ) {
        memberService.join(username, password, name, email, nickname, age, region, dailyTarget);
        return "redirect:/login?msg=Welcome";
    }

    // 아이디 중복 체크 (AJAX)
    @GetMapping("/checkUsername")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = memberService.isUsernameTaken(username);
        response.put("result", exists ? "fail" : "success");
        return response;
    }

    // 닉네임 중복 체크 (AJAX)
    @GetMapping("/checkNickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = memberService.isNicknameTaken(nickname);
        response.put("result", exists ? "fail" : "success");
        return response;
    }

    // 로그인 정보 검증
    @PostMapping("/validLoginInfo")
    @ResponseBody
    public ResultData<Map<String, Object>> validLoginInfo(@RequestParam String username,
                                                          @RequestParam String password) {
        return memberService.validateLoginInfo(username.trim(), password);
    }

    // 탈퇴 계정 복구
    @PostMapping("/restore")
    @ResponseBody
    public ResultData<Map<String, Object>> restoreAccount(@RequestParam String username,
                                                          @RequestParam String password) {
        return memberService.restore(username.trim(), password);
    }

    // 아이디 찾기 페이지
    @GetMapping("/findLoginId")
    public String findLoginId() {
        return "usr/member/findLoginId";
    }

    // 아이디 찾기 처리
    @GetMapping("/doFindLoginId")
    @ResponseBody
    public ResultData<Map<String, Object>> doFindLoginId(@RequestParam String name,
                                                         @RequestParam String email) {
        return memberService.findLoginIdByNameAndEmail(name.trim(), email.trim());
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/findLoginPw")
    public String findLoginPw() {
        return "usr/member/findLoginPw";
    }

    // 비밀번호 찾기 처리 (임시 비밀번호 발급)
    @GetMapping("/doFindLoginPw")
    @ResponseBody
    public ResultData<Map<String, Object>> doFindLoginPw(@RequestParam String loginId,
                                                         @RequestParam String email) {
        return memberService.resetPasswordWithEmail(loginId.trim(), email.trim());
    }

    // 마이페이지
    @GetMapping("/myPage")
    public String myPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        memberService.findByUsername(principal.getName())
                .ifPresent(member -> {
                    model.addAttribute("member", member);
                    LocalDateTime last = member.getNicknameUpdatedAt();
                    LocalDateTime next = (last != null) ? last.plusDays(30) : null;
                    boolean nicknameChangeAllowed = (next == null) || !next.isAfter(LocalDateTime.now());
                    String nextNicknameChangeDate = (next != null)
                            ? next.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))
                            : "";
                    long nicknameDaysLeft = 0;
                    if (next != null && next.isAfter(LocalDateTime.now())) {
                        nicknameDaysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), next);
                        if (nicknameDaysLeft < 0) nicknameDaysLeft = 0;
                    }
                    model.addAttribute("nicknameChangeAllowed", nicknameChangeAllowed);
                    model.addAttribute("nextNicknameChangeDate", nextNicknameChangeDate);
                    model.addAttribute("nicknameDaysLeft", nicknameDaysLeft);
                });
        return "usr/member/myPage";
    }

    // 닉네임 변경
    @PostMapping("/modifyNickname")
    @ResponseBody
    public ResultData<Map<String, Object>> modifyNickname(@RequestParam String nickname, Principal principal) {
        if (principal == null) {
            return new ResultData<>("F-0", "로그인이 필요합니다.");
        }
        return memberService.updateNickname(principal.getName(), nickname.trim());
    }

    // 비밀번호 변경
    @PostMapping("/modifyPassword")
    @ResponseBody
    public ResultData<Map<String, Object>> modifyPassword(@RequestParam String password, Principal principal) {
        if (principal == null) {
            return new ResultData<>("F-0", "로그인이 필요합니다.");
        }
        return memberService.updatePassword(principal.getName(), password);
    }

    // 프로필 변경 (닉네임/이메일/지역)
    @PostMapping("/modifyProfile")
    @ResponseBody
    public ResultData<Map<String, Object>> modifyProfile(@RequestParam String nickname,
                                                         @RequestParam String email,
                                                         @RequestParam String region,
                                                         @RequestParam int dailyTarget,
                                                         Principal principal) {
        if (principal == null) {
            return new ResultData<>("F-0", "로그인이 필요합니다.");
        }
        return memberService.updateProfile(principal.getName(), nickname.trim(), email.trim(), region.trim(), dailyTarget);
    }

    // 회원 탈퇴 (7일 보관)
    @PostMapping("/withdraw")
    @ResponseBody
    public ResultData<Map<String, Object>> withdraw(Principal principal) {
        if (principal == null) {
            return new ResultData<>("F-0", "로그인이 필요합니다.");
        }
        return memberService.withdraw(principal.getName());
    }
}
