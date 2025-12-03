package com.mmb.controller;

import com.mmb.service.MemberService;
import com.mmb.service.ArticleService;
import com.mmb.repository.StudyRecordRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usr/home")
@RequiredArgsConstructor
public class UsrHomeController {

    private final MemberService memberService;
    private final StudyRecordRepository studyRecordRepository;
    private final ArticleService articleService; // 강사님 프로젝트에서 쓰던 서비스 재사용한다고 가정

    @GetMapping("/main")
    public String showMain(HttpSession session, Model model) {

        Long loginedMemberId = (Long) session.getAttribute("loginedMemberId");

        if (loginedMemberId != null) {
            memberService.findById(loginedMemberId).ifPresent(member -> {
                model.addAttribute("member", member);
                // TODO: 오늘 학습 요약, 포인트, 레벨 등은 나중에 여기에서 계산
            });
        }

        // 🔹 공지사항 최신 3개만 메인에 띄워주기
        int noticeBoardId = 1;
        var notices = articleService.findLatestArticles(noticeBoardId, 3);
        // ↑ 이 메서드는 강사님 list 로직을 응용해서 직접 만들면 됨 (예: boardId + limit로 조회)
        model.addAttribute("notices", notices);

        // 🔹 랭킹 (일단 TODO로 두고 나중에 구현)
        // var ranking = studyRecordRepository.getTopRanking(5);
        // model.addAttribute("ranking", ranking);

        return "usr/home/main";
    }
}
