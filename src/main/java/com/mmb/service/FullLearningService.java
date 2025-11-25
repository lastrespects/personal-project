package com.mmb.service;

import com.mmb.domain.*;
import com.mmb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final StudyRecordRepository studyRecordRepository;

    // 1. 오늘의 퀴즈 생성 (복습 + 신규 = 목표량)
    @Transactional
    public List<StudyRecord> generateDailyQuiz(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        int target = member.getDailyTarget(); // 사용자가 설정한 목표 (예: 30개)

        // A. 복습 단어 가져오기
        List<StudyRecord> quizList = studyRecordRepository.findTodayReviews(memberId, LocalDate.now());
        
        // B. 목표량이 부족하면 신규 단어 추가
        if (quizList.size() < target) {
            int needed = target - quizList.size();
            List<Word> newWords = fetchNewWordsFromApi(needed); // API 호출
            
            for (Word w : newWords) {
                // DB 중복 체크 후 저장
                if (!wordRepository.existsBySpelling(w.getSpelling())) {
                    wordRepository.save(w);
                }
                // 학습 기록 생성 (중복 방지)
                if (!studyRecordRepository.existsByMemberAndWord(member, w)) {
                    StudyRecord newRecord = new StudyRecord(member, w);
                    studyRecordRepository.save(newRecord);
                    quizList.add(newRecord);
                }
            }
        }
        return quizList; // 최종 퀴즈 리스트 반환
    }

    // 2. 문제 채점 (점수 부여 로직)
    @Transactional
    public String gradeAnswer(Long recordId, String userAnswer) {
        StudyRecord record = studyRecordRepository.findById(recordId).orElseThrow();
        Member member = record.getMember();
        Word word = record.getWord();

        boolean isCorrect = word.getSpelling().equalsIgnoreCase(userAnswer);

        if (isCorrect) {
            // 정답 처리: 망각곡선 적용 (날짜 미루기)
            int step = record.getReviewStep() + 1;
            int days = (int) Math.pow(2, step); // 2, 4, 8일...
            
            record.setReviewStep(step);
            record.setNextReviewDate(LocalDate.now().plusDays(days));
            
            // ★ 포인트 지급 (단어 1개당 10점)
            member.gainExp(10); 
            
            return "정답! 경험치 +10 (다음 복습: " + days + "일 뒤)";
        } else {
            // 오답 처리: 1단계 초기화 & 내일 다시
            record.setReviewStep(0);
            record.setWrongCount(record.getWrongCount() + 1);
            record.setNextReviewDate(LocalDate.now().plusDays(1)); // 내일
            
            return "오답.. 내일 다시 공부하세요.";
        }
    }

    // 3. 힌트 사용 (하루 1회 제한)
    @Transactional
    public String useHint(Long memberId, Long wordId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();
        
        // 오늘 날짜 확인
        if (LocalDate.now().equals(member.getLastHintDate())) {
            return "실패: 오늘은 이미 힌트를 사용했습니다.";
        }

        // 힌트 제공 (첫 글자 보여주기 or 중간 글자)
        member.setLastHintDate(LocalDate.now()); // 사용 기록 저장
        return "힌트: 첫 글자는 [" + word.getSpelling().charAt(0) + "] 입니다.";
    }

    // (가짜 API - 실제로는 여기에 RestTemplate 코드 삽입)
    private List<Word> fetchNewWordsFromApi(int count) {
        List<Word> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long rnd = System.currentTimeMillis() % 10000 + i;
            // 예문(빈칸) 포함 데이터 생성
            list.add(new Word("word" + rnd, "뜻" + rnd, "This is a sample sentence for _____ ."));
        }
        return list;
    }
}