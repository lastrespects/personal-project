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
    public String record(@RequestParam Integer wordId,
            @RequestParam(defaultValue = "true") boolean correct,
            @RequestParam(defaultValue = "BOOK") String studyType) {

        Integer memberId = getLoginMemberId(); // TODO: 로그인 연동 필요

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

    private Integer getLoginMemberId() {
        // TODO: 실제 세션 값을 사용하도록 개선 필요. 현재는 1 고정
        return 1;
    }
}
