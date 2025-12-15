package com.mmb.controller;

import com.mmb.service.ArticleService;
import com.mmb.service.FullLearningService;
import com.mmb.service.MemberService;
import com.mmb.util.SessionMemberUtil;
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
        Integer loginedMemberId = SessionMemberUtil.getSessionMemberId(session);

        // 세션에 loginedMemberId 없지만 principal이 있으면 세팅
        if (loginedMemberId == null && principal != null) {
            memberService.findByUsername(principal.getName()).ifPresent(member -> {
                session.setAttribute("loginedMemberId", member.getId());
            });
            loginedMemberId = SessionMemberUtil.getSessionMemberId(session);
        }

        // 로그인/비로그인 공통 데이터
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

        // ✅ 공지 최신글
        int noticeBoardId = 1;
        var notices = articleService.findLatestArticles(noticeBoardId, 3);
        model.addAttribute("notices", notices);

        // ✅ Q&A 최신글 (공지 자리로 내려보낼 거)
        int qnaBoardId = 2;
        var qnas = articleService.findLatestArticles(qnaBoardId, 3);
        model.addAttribute("qnas", qnas);

        return "usr/home/main";
    }
}
