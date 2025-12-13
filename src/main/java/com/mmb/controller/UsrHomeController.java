package com.mmb.controller;

import com.mmb.service.ArticleService;
import com.mmb.service.FullLearningService;
import com.mmb.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/usr/home")
@RequiredArgsConstructor
public class UsrHomeController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final FullLearningService fullLearningService;

    @GetMapping("/main")
    public String showMain(HttpSession session, Model model, Principal principal) {
        Long loginedMemberId = (Long) session.getAttribute("loginedMemberId");

        if (loginedMemberId == null && principal != null) {
            memberService.findByUsername(principal.getName()).ifPresent(member -> {
                session.setAttribute("loginedMemberId", member.getId());
            });
            loginedMemberId = (Long) session.getAttribute("loginedMemberId");
        }

        if (loginedMemberId != null) {
            memberService.findById(loginedMemberId).ifPresent(member -> model.addAttribute("member", member));
            long todayLearnedCount = fullLearningService.getTodayLearnedCount(loginedMemberId);
            int todayTarget = fullLearningService.getDailyTarget(loginedMemberId);
            long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(loginedMemberId);
            long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);

            model.addAttribute("todayLearnedCount", todayLearnedCount);
            model.addAttribute("todayTarget", todayTarget);
            model.addAttribute("quizSolvedCount", quizSolvedCount);
            model.addAttribute("quizRemainingCount", quizRemainingCount);
        } else {
            model.addAttribute("todayLearnedCount", 0);
            model.addAttribute("todayTarget", 30);
            model.addAttribute("quizSolvedCount", 0);
            model.addAttribute("quizRemainingCount", 30);
        }

        int noticeBoardId = 1;
        var notices = articleService.findLatestArticles(noticeBoardId, 3);
        model.addAttribute("notices", notices);

        return "usr/home/main";
    }
}
