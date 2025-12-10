package com.mmb.controller;

import com.mmb.entity.Word;
import com.mmb.service.ArticleService;
import com.mmb.service.FullLearningService;
import com.mmb.service.MemberService;
import com.mmb.repository.StudyRecordRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/usr/home")
@RequiredArgsConstructor
public class UsrHomeController {

    private final MemberService memberService;
    private final StudyRecordRepository studyRecordRepository;
    private final ArticleService articleService;
    private final FullLearningService fullLearningService;

    @GetMapping("/main")
    public String showMain(HttpSession session, Model model, Principal principal) {

        Long loginedMemberId = (Long) session.getAttribute("loginedMemberId");

        if (loginedMemberId != null) {
            memberService.findById(loginedMemberId).ifPresent(member -> {
                model.addAttribute("member", member);
                // TODO: 오늘 학습 예약, 미션/알림 계산
            });
        }

        // 세션에 값이 없거나 모델에 아직 member가 없으면 principal 기반으로 재조회
        if (model.getAttribute("member") == null && principal != null) {
            memberService.findByUsername(principal.getName()).ifPresent(member -> {
                model.addAttribute("member", member);
                session.setAttribute("loginedMemberId", member.getId());
                // Update loginedMemberId for use below
            });
            // Re-fetch ID if it was just set
            if (loginedMemberId == null) {
                loginedMemberId = (Long) session.getAttribute("loginedMemberId");
            }
        }

        // ✅ Add Today's Learning Stats
        // ✅ Add Today's Learning Stats
        if (loginedMemberId != null) {
            long todayLearnedCount = fullLearningService.getTodayLearnedCount(loginedMemberId);
            int todayTarget = fullLearningService.getDailyTarget(loginedMemberId);

            // 퀴즈 단어 수는 실제 생성된 리스트 크기 (목표와 다를 수 있음 - 부족할 경우 등)
            // 하지만 UI에서는 목표를 보여주는 것이 요청사항

            model.addAttribute("todayLearnedCount", todayLearnedCount);
            model.addAttribute("todayTarget", todayTarget);
            model.addAttribute("todayQuizCount", todayTarget); // 퀴즈 문제 수도 목표와 동일하게 표시
        } else {
            model.addAttribute("todayLearnedCount", 0);
            model.addAttribute("todayTarget", 30);
            model.addAttribute("todayQuizCount", 0);
        }

        // 메인 공지사항 최신 3개
        int noticeBoardId = 1;
        var notices = articleService.findLatestArticles(noticeBoardId, 3);
        model.addAttribute("notices", notices);

        // 랭킹은 TODO
        // var ranking = studyRecordRepository.getTopRanking(5);
        // model.addAttribute("ranking", ranking);

        return "usr/home/main";
    }
}
