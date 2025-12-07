package com.mmb.controller;

import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.service.LearningService;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/learning")
@RequiredArgsConstructor
public class UsrLearningController {

    private final LearningService learningService;
    private final MemberService memberService;

    @GetMapping("/wordbook")
    public String showWordbook(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        String username = principal.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        return "usr/learning/wordbook";
    }

    @GetMapping({"/quiz", "/today"})
    public String showQuiz(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        String username = principal.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        return "usr/learning/today";
    }
}
