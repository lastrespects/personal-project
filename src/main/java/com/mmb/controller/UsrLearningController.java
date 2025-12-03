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

    @GetMapping("/today")
    public String showTodayLearning(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        String username = principal.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        return "usr/learning/today";
    }
}
