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
    public String recordQuiz(@RequestParam Integer wordId,
            @RequestParam boolean correct) {

        Integer memberId = getLoginMemberId(); // TODO: 로그인 세션 연동 필요
        fullLearningService.applyQuizResult(memberId, wordId, correct);
        return "OK";
    }

    private Integer getLoginMemberId() {
        // TODO: 세션/보안 컨텍스트 기반으로 연동. 현재는 테스트용 1 고정
        return 1;
    }
}
