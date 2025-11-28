package com.mmb.controller;

import com.mmb.dto.MemberCreateDto;
import com.mmb.dto.MemberResponseDto;
import com.mmb.dto.StudyRecordDto;
import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학습 관련 API 엔드포인트
 * - /api/join : 회원가입
 * - /api/start/{memberId} : 오늘의 퀴즈
 * - /api/study/{studyRecordId}/wrong : 틀림 처리
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {

    private final FullLearningService fullLearningService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody MemberCreateDto dto) {
        try {
            var saved = fullLearningService.join(dto);
            return ResponseEntity.ok(MemberResponseDto.from(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/start/{memberId}")
    public ResponseEntity<?> start(@PathVariable Long memberId) {
        try {
            List<StudyRecordDto> quiz = fullLearningService.generateDailyQuizDto(memberId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/study/{studyRecordId}/wrong")
    public ResponseEntity<?> markWrong(@PathVariable Long studyRecordId) {
        try {
            fullLearningService.markWrong(studyRecordId);
            return ResponseEntity.ok("marked wrong");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
