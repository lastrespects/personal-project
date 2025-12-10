package com.mmb.controller;

import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudyRecordController {

    private final FullLearningService fullLearningService;

    @PostMapping("/usr/study/record")
    public String record(@RequestParam Long wordId,
            @RequestParam(defaultValue = "true") boolean correct,
            @RequestParam(defaultValue = "BOOK") String studyType) {

        Long memberId = getLoginMemberId(); // ✅ 네 방식

        fullLearningService.recordStudy(memberId, wordId, correct, studyType);

        return "OK";
    }

    private Long getLoginMemberId() {
        // TODO: 구현 교체
        return 1L;
    }
}
