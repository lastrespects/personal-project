package com.mmb.controller;

import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyRecordController {

    private final FullLearningService fullLearningService;

    @PostMapping("/usr/study/record")
    public String record(@RequestParam Long wordId,
            @RequestParam(defaultValue = "true") boolean correct,
            @RequestParam(defaultValue = "BOOK") String studyType) {

        Long memberId = getLoginMemberId(); // TODO: 로그인 연동 필요

        String normalizedType = studyType == null ? "BOOK" : studyType.trim().toUpperCase();
        if ("QUIZ".equals(normalizedType)) {
            fullLearningService.applyQuizResult(memberId, wordId, correct);
        } else if ("BOOK".equals(normalizedType)) {
            fullLearningService.recordStudy(memberId, wordId, true, "BOOK");
        } else {
            fullLearningService.recordStudy(memberId, wordId, correct, normalizedType);
        }

        return "OK";
    }

    private Long getLoginMemberId() {
        // TODO: 구현 교체
        return 1L;
    }
}
