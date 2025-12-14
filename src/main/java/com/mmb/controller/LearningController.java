// src/main/java/com/mmb/controller/LearningController.java
package com.mmb.controller;

import com.mmb.dto.LearningResultRequest;
import com.mmb.dto.TodayWordDto;
import com.mmb.service.LearningService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    /**
     * 오늘 학습할 단어 목록
     * 예: GET /api/learning/today?memberId=1
     */
    @GetMapping("/today")
    public List<TodayWordDto> getTodayWords(@RequestParam Integer memberId) {
        return learningService.prepareTodayWords(memberId);
    }

    /**
     * 한 단어에 대한 학습 결과 전송
     * 예: POST /api/learning/result?memberId=1
     * body: { "wordId": 10, "correct": true }
     */
    @PostMapping("/result")
    public void submitResult(
            @RequestParam Integer memberId,
            @RequestBody LearningResultRequest request
    ) {
        learningService.recordResult(memberId, request.getWordId(), request.isCorrect());
    }
}
