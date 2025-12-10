package com.mmb.controller;

import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuizRecordController {

    private final FullLearningService fullLearningService;

    @PostMapping("/usr/learning/record-quiz")
    public String recordQuiz(@RequestParam Long wordId,
            @RequestParam boolean correct) {

        Long memberId = getLoginMemberId(); // ✅ 프로젝트 로그인 방식으로 교체
        fullLearningService.applyQuizResult(memberId, wordId, correct);
        return "OK";
    }

    private Long getLoginMemberId() {
        // TODO: 세션/시큐리티 기반으로 교체. 현재는 하드코딩 1L
        return 1L;
    }
}
