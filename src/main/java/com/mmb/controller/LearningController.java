package com.mmb.controller;

import com.mmb.domain.Member;
import com.mmb.domain.StudyRecord;
import com.mmb.repository.MemberRepository;
import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {

    private final MemberRepository memberRepository;
    private final FullLearningService fullLearningService;

    // 1. 회원 가입 및 로그인 (Member 생성) API
    // URL: POST http://localhost:8080/api/join
    // Body: JSON {"username": "user1", "nickname": "final_user", "dailyTarget": 30}
    @PostMapping("/join")
    public Member createMember(@RequestBody Member member) {
        // 이미 존재하는 사용자인지 확인 (간단한 로그인 로직으로 대체)
        Member existingMember = memberRepository.findByUsername(member.getUsername());
        if (existingMember != null) {
            // 이미 있으면 기존 사용자 정보 반환 (로그인 성공)
            return existingMember; 
        }

        // 새로운 사용자 생성 및 저장 (회원 가입)
        member.setDailyTarget(member.getDailyTarget() > 0 ? member.getDailyTarget() : 30); // 목표가 없으면 기본 30
        member.setCharacterLevel(1);
        member.setCurrentExp(0);
        
        return memberRepository.save(member);
    }
    
    // 2. 오늘의 퀴즈 시작 API
    // URL: GET http://localhost:8080/api/start/{memberId}
    @GetMapping("/start/{memberId}")
    public List<StudyRecord> startDailyQuiz(@PathVariable Long memberId) {
        return fullLearningService.generateDailyQuiz(memberId);
    }

    // 3. 문제 채점 API (나중에 구현)
    // URL: POST http://localhost:8080/api/grade/{recordId}
    /*
    @PostMapping("/grade/{recordId}")
    public String gradeAnswer(@PathVariable Long recordId, @RequestBody AnswerRequest request) {
        return fullLearningService.gradeAnswer(recordId, request.getAnswer());
    }
    */
}