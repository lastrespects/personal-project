package com.mmb.controller;

import com.mmb.dto.Req;
import com.mmb.dto.StudyRecordDto;
import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 학습 관련 API 엔드포인트
 * - /api/start : 오늘의 퀴즈
 * - /api/study/{studyRecordId}/wrong : 틀림 처리
 * - /api/study/{studyRecordId}/like : 좋아요/취소 처리
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {

    private final FullLearningService fullLearningService;
    private final Req req; // 세션 관리를 위해 Req 객체 주입

    /**
     * 오늘의 퀴즈를 생성하고 조회합니다. (로그인 필요)
     */
    @GetMapping("/start")
    public ResponseEntity<?> start() {
        if (req.getLoginedMember().getId() == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }
        
        try {
            int memberId = req.getLoginedMember().getId();
            List<StudyRecordDto> quiz = fullLearningService.generateDailyQuizDto(memberId);
            return ResponseEntity.ok(quiz);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("퀴즈 생성 중 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 퀴즈 문제 틀림 처리 (로그인 필요)
     */
    @PostMapping("/study/{studyRecordId}/wrong")
    public ResponseEntity<?> markWrong(@PathVariable int studyRecordId) {
        if (req.getLoginedMember().getId() == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }
        try {
            fullLearningService.markWrong(studyRecordId);
            return ResponseEntity.ok("marked wrong");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * 퀴즈 문제(StudyRecord)에 좋아요를 누르거나 취소합니다. (로그인 필요)
     */
    @PostMapping("/study/{studyRecordId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable int studyRecordId) {
        if (req.getLoginedMember().getId() == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }
        
        try {
            int memberId = req.getLoginedMember().getId();
            String result = fullLearningService.toggleLikeStudyRecord(memberId, studyRecordId);
            
            return ResponseEntity.ok(result); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("좋아요 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
}