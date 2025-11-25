package com.mmb.controller;

import com.mmb.domain.*;
import com.mmb.repository.MemberRepository;
import com.mmb.service.FullLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {

    private final FullLearningService learningService;
    private final MemberRepository memberRepository;

    // 1. 학습 시작 (오늘의 단어 30개 받아오기)
    @GetMapping("/start/{memberId}")
    public List<StudyRecord> startLearning(@PathVariable Long memberId) {
        return learningService.generateDailyQuiz(memberId);
    }

    // 2. 정답 제출
    @PostMapping("/submit")
    public String submitAnswer(@RequestParam Long recordId, @RequestParam String answer) {
        return learningService.gradeAnswer(recordId, answer);
    }

    // 3. 힌트 요청
    @PostMapping("/hint")
    public String getHint(@RequestParam Long memberId, @RequestParam Long wordId) {
        return learningService.useHint(memberId, wordId);
    }
    
 // 4. (GET) 랭킹 확인 - 이 주소는 브라우저로 바로 확인 가능합니다.
    @GetMapping("/rank") 
    public List<Member> getRanking() {
        return memberRepository.findTop10ByOrderByCurrentExpDesc();
    }

    // 5. (POST) 사용자 생성 - 이 주소는 브라우저로 바로 접근하면 405 에러가 납니다!
    @PostMapping("/join")
//    @GetMapping("/join")
    public Member join(@RequestParam String name, @RequestParam int target) {
        Member m = new Member("user"+System.currentTimeMillis(), name, target);
        return memberRepository.save(m);
    }
}