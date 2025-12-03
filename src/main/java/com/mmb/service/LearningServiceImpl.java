// src/main/java/com/mmb/service/learning/LearningServiceImpl.java
package com.mmb.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final MemberRepository memberRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final WordGenerationService wordGenerationService;

    @Override
    @Transactional
    public List<TodayWordDto> prepareTodayWords(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID=" + memberId));

        int targetCount = Optional.ofNullable(member.getDailyTarget()).orElse(30);

        // 1) 해당 회원의 모든 학습 기록 조회
        List<StudyRecord> allRecords = studyRecordRepository.findByMemberId(memberId);

        // 2) 오늘 복습해야 할 단어 추출 (SRS)
        List<StudyRecord> dueRecords = allRecords.stream()
                .filter(this::isDueForReview)
                .sorted(this::comparePriority)
                .limit(targetCount)
                .toList();

        List<TodayWordDto> result = new ArrayList<>();

        for (StudyRecord record : dueRecords) {
            Word w = record.getWord();
            result.add(TodayWordDto.builder()
                    .wordId(w.getId())
                    .spelling(w.getSpelling())
                    .meaning(w.getMeaning())
                    .exampleSentence(w.getExampleSentence())
                    .audioPath(w.getAudioPath())
                    .review(true)
                    .build());
        }

        int remain = targetCount - result.size();

        // 3) 부족하면 새 단어 생성
        if (remain > 0) {
            List<Word> newWords = wordGenerationService.generateNewWordsForMember(member, remain);
            for (Word w : newWords) {

                StudyRecord record = StudyRecord.builder()
                        .member(member)
                        .word(w)
                        .correctCount(0)
                        .incorrectCount(0)
                        .totalAttempts(0)
                        .lastReviewDate(null)
                        .build();
                studyRecordRepository.save(record);

                result.add(TodayWordDto.builder()
                        .wordId(w.getId())
                        .spelling(w.getSpelling())
                        .meaning(w.getMeaning())
                        .exampleSentence(w.getExampleSentence())
                        .audioPath(w.getAudioPath())
                        .review(false)
                        .build());
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void recordResult(Long memberId, Long wordId, boolean correct) {
        StudyRecord record = studyRecordRepository
                .findByMemberIdAndWordId(memberId, wordId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "학습 기록이 없습니다. memberId=" + memberId + ", wordId=" + wordId));

        record.setTotalAttempts(record.getTotalAttempts() + 1);
        if (correct) {
            record.setCorrectCount(record.getCorrectCount() + 1);
        } else {
            record.setIncorrectCount(record.getIncorrectCount() + 1);
        }
        record.setLastReviewDate(LocalDateTime.now());
    }

    // =============================
    // SRS 관련 내부 로직
    // =============================

    /**
     * 이 단어가 오늘 복습 대상인지 여부
     */
    private boolean isDueForReview(StudyRecord record) {
        if (record.getLastReviewDate() == null || record.getTotalAttempts() == 0) {
            return false; // 아직 본 적 없는 단어는 "복습 대상" 아님
        }

        int correct = record.getCorrectCount();
        int incorrect = record.getIncorrectCount();
        int score = correct - incorrect;

        int intervalDays;
        if (score <= 0) {
            intervalDays = 1; // 어려운 단어: 하루마다
        } else if (score <= 2) {
            intervalDays = 2;
        } else if (score <= 4) {
            intervalDays = 4;
        } else {
            intervalDays = 7; // 충분히 익힌 단어: 7일마다
        }

        LocalDateTime nextReviewDate = record.getLastReviewDate().plusDays(intervalDays);
        return !nextReviewDate.isAfter(LocalDateTime.now());
    }

    /**
     * 어떤 단어를 먼저 보여줄지 정렬 기준
     */
    private int comparePriority(StudyRecord a, StudyRecord b) {
        int scoreA = a.getCorrectCount() - a.getIncorrectCount();
        int scoreB = b.getCorrectCount() - b.getIncorrectCount();

        if (scoreA != scoreB) {
            return Integer.compare(scoreA, scoreB); // 점수 낮은(힘든) 단어 먼저
        }

        LocalDateTime dateA = a.getLastReviewDate();
        LocalDateTime dateB = b.getLastReviewDate();

        if (dateA == null && dateB != null)
            return -1;
        if (dateA != null && dateB == null)
            return 1;
        if (dateA == null)
            return 0;

        return dateA.compareTo(dateB);
    }
}
