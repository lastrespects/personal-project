package com.mmb.controller;

import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizRecordController {

    private final FullLearningService fullLearningService;

    @PostMapping("/usr/learning/record-quiz")
    public String recordQuiz(@RequestParam Long wordId,
            @RequestParam boolean correct) {

        Long memberId = getLoginMemberId(); // TODO: 로그인 세션 값으로 교체
        fullLearningService.applyQuizResult(memberId, wordId, correct);
        return "OK";
    }

    private Long getLoginMemberId() {
        // TODO: 세션/보안 컨텍스트 기반으로 교체. 현재는 임시 1L
        return 1L;
    }
}
