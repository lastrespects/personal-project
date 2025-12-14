package com.mmb.service;

import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import com.mmb.entity.WordProgress;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;
import com.mmb.repository.WordProgressRepository;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningWriteService {

    private final StudyRecordRepository studyRecordRepository;
    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final WordProgressRepository wordProgressRepository;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void recordStudy(Integer memberId, Integer wordId, boolean correct, String studyType) {
        validateIds(memberId, wordId);
        logWriteState("STUDY_RECORD_INSERT", memberId, wordId);

        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        StudyRecord record = StudyRecord.builder()
                .memberId(member.getId())
                .wordId(word.getId())
                .studiedAt(now)
                .studyType(studyType)
                .correct(correct)
                .build();

        studyRecordRepository.save(record);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void applyQuizResult(Integer memberId, Integer wordId, boolean correct) {
        validateIds(memberId, wordId);
        logWriteState("QUIZ_RESULT_SAVE", memberId, wordId);

        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        studyRecordRepository.save(StudyRecord.builder()
                .memberId(member.getId())
                .wordId(word.getId())
                .studiedAt(now)
                .studyType("QUIZ")
                .correct(correct)
                .build());

        WordProgress wp = wordProgressRepository
                .findByMemberIdAndWordId(memberId, wordId)
                .orElseGet(() -> WordProgress.builder()
                        .memberId(memberId)
                        .wordId(wordId)
                        .wrongStreak(0)
                        .correctCount(0)
                        .wrongCount(0)
                        .build());

        if (wp.getMemberId() == null || wp.getWordId() == null) {
            throw new IllegalStateException("WordProgress memberId/wordId is null");
        }

        wp.setLastStudiedDate(LocalDate.now());

        if (correct) {
            wp.setCorrectCount(wp.getCorrectCount() + 1);
            wp.setWrongStreak(0);
            wp.setNextReviewDate(LocalDate.now().plusDays(10));
        } else {
            wp.setWrongCount(wp.getWrongCount() + 1);
            wp.setWrongStreak(wp.getWrongStreak() + 1);

            int days = wrongIntervalDays(wp.getWrongStreak());
            wp.setNextReviewDate(LocalDate.now().plusDays(days));
        }

        wordProgressRepository.save(wp);
    }

    private void validateIds(Integer memberId, Integer wordId) {
        if (memberId == null || memberId <= 0 || wordId == null || wordId <= 0) {
            throw new IllegalArgumentException("invalid memberId/wordId");
        }
    }

    private int wrongIntervalDays(int wrongStreak) {
        if (wrongStreak <= 1)
            return 7;
        if (wrongStreak == 2)
            return 5;
        if (wrongStreak == 3)
            return 3;
        return 1;
    }

    private void logWriteState(String action, Integer memberId, Integer wordId) {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug("[WRITE_TRY] action={} memberId={} wordId={} txActive={} txReadOnly={}",
                action, memberId, wordId, txActive, txReadOnly);
    }
}
