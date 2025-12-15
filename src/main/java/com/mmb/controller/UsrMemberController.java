package com.mmb.controller;

import com.mmb.dto.ResultData;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

    // ✅ 자동로그인에 필요
    private final AuthenticationConfiguration authenticationConfiguration;

    // 회원가입 페이지
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    // ✅ 회원가입 처리 + 자동로그인 + 환영 alert용 msg 전달
    @PostMapping("/doJoin")
    public String doJoin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestParam int age,
            @RequestParam String region,
            @RequestParam int dailyTarget,
            HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        // 1) 회원가입
        memberService.join(username, password, name, email, nickname, age, region, dailyTarget);

        // 2) 자동로그인 (Spring Security 인증 후 세션에 SecurityContext 저장)
        try {
            AuthenticationManager am = authenticationConfiguration.getAuthenticationManager();

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

            Authentication auth = am.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context);

        } catch (Exception e) {
            // 자동로그인 실패 시 로그인 페이지로 보내면서 msg만 띄움(폴백)
            ra.addFlashAttribute("msg", nickname + "님을 환영합니다. 로그인해 주세요.");
            return "redirect:/login";
        }

        // 3) 자동로그인 성공: 메인으로 보내면서 msg 전달 (메인에서 alert 띄우기)
        ra.addFlashAttribute("msg", nickname + "님을 환영합니다.");
        return "redirect:/usr/home/main";
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
    public ResultData<Map<String, Object>> validLoginInfo(
            @RequestParam String username,
            @RequestParam String password) {
        return memberService.validateLoginInfo(username.trim(), password);
    }

    // 탈퇴 계정 복구
    @PostMapping("/restore")
    @ResponseBody
    public ResultData<Map<String, Object>> restoreAccount(
            @RequestParam String username,
            @RequestParam String password) {
        return memberService.restore(username.trim(), password);
    }

    // 아이디 찾기 페이지
    @GetMapping("/findLoginId")
    public String findLoginId() {
        return "usr/member/findLoginId";
    }

    // 아이디 찾기 처리 (GET/POST 모두 허용)
    @RequestMapping(value = "/doFindLoginId", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public ResultData<Map<String, Object>> doFindLoginId(
            @RequestParam String name,
            @RequestParam String email) {
        return memberService.findLoginIdByNameAndEmail(name.trim(), email.trim());
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/findLoginPw")
    public String findLoginPw() {
        return "usr/member/findLoginPw";
    }

    // 비밀번호 찾기 처리 (GET/POST 모두 허용)
    @RequestMapping(value = "/doFindLoginPw", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public ResultData<Map<String, Object>> doFindLoginPw(
            @RequestParam String loginId,
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
                        if (nicknameDaysLeft < 0)
                            nicknameDaysLeft = 0;
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

    // 프로필 변경 (닉네임/이메일/지역/목표량)
    @PostMapping("/modifyProfile")
    @ResponseBody
    public ResultData<Map<String, Object>> modifyProfile(
            @RequestParam String nickname,
            @RequestParam String email,
            @RequestParam String region,
            @RequestParam int dailyTarget,
            Principal principal) {
        if (principal == null) {
            return new ResultData<>("F-0", "로그인이 필요합니다.");
        }
        return memberService.updateProfile(
                principal.getName(),
                nickname.trim(),
                email.trim(),
                region.trim(),
                dailyTarget);
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
