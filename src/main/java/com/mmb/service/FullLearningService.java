package com.mmb.service;

import com.mmb.domain.StudyRecord;
import com.mmb.domain.Word;
import com.mmb.dto.Member;
import com.mmb.dto.StudyRecordDto;
import com.mmb.dto.WordDto;
import com.mmb.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 학습 전체 로직 - 최적화 버전 (비동기 처리, MyBatis 멤버/좋아요 연동)
 */
@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final MemberService memberService; // [MyBatis 연동]
    private final StudyRecordRepository studyRecordRepository; // [JPA 유지]
    private final LikePointService likePointService; // [MyBatis 연동]
    private final WordService wordService;

    // NOTE: 기존의 `join` 메서드는 UsrMemberController의 doJoin으로 대체되었습니다.

    /**
     * 일일 퀴즈 생성 (비동기 처리)
     */
    @Transactional(readOnly = true)
    public List<StudyRecordDto> generateDailyQuizDto(int memberId) {
        
        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Invalid member ID");
        }
        
        int dailyTarget = member.getDailyTarget();
        
        List<StudyRecord> today = studyRecordRepository.findTodayReviews(memberId, LocalDate.now());
        int remaining = Math.max(0, dailyTarget - today.size());

        if (remaining > 0) {
            List<String> randomWords = fetchRandomWords(Math.max(remaining * 2, remaining + 5));

            List<CompletableFuture<Optional<Word>>> futures = new ArrayList<>();
            for (String sp : randomWords) {
                futures.add(findOrCreateAsync(sp));
            }

            for (CompletableFuture<Optional<Word>> f : futures) {
                try {
                    Optional<Word> opt = f.get();
                    if (opt.isPresent()) {
                        Word w = opt.get();
                        if (!studyRecordRepository.existsByMemberIdAndWord(memberId, w) && today.size() < dailyTarget) {
                            StudyRecord rec = StudyRecord.builder()
                                    .memberId(memberId) 
                                    .word(w)
                                    .reviewStep(0)
                                    .wrongCount(0)
                                    .nextReviewDate(LocalDate.now())
                                    .build();
                            studyRecordRepository.save(rec);
                            today.add(rec);
                        }
                        if (today.size() >= dailyTarget) break;
                    }
                } catch (Exception e) {
                    System.err.println("비동기 단어 처리 실패: " + e.getMessage());
                }
            }
        }

        List<StudyRecordDto> result = new ArrayList<>();
        for (StudyRecord r : today) {
            Word w = r.getWord();
            WordDto wd = new WordDto(w.getSpelling(), w.getMeaning(), w.getExampleSentence(), w.getAudioPath());
            result.add(new StudyRecordDto(r.getId(), wd, r.getReviewStep(), r.getWrongCount(), r.getNextReviewDate()));
        }

        return result;
    }

    // ... (fetchRandomWords, findOrCreateAsync 메서드는 이전 코드와 동일) ...
    private List<String> fetchRandomWords(int number) { /* ... 코드 유지 ... */ return List.of(); }
    @Async
    @Transactional
    public CompletableFuture<Optional<Word>> findOrCreateAsync(String spelling) { /* ... 코드 유지 ... */ return CompletableFuture.completedFuture(Optional.empty()); }
    
    /**
     * 틀림 처리
     */
    @Transactional
    public void markWrong(int studyRecordId) {
        StudyRecord rec = studyRecordRepository.findById((long)studyRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid study record id"));

        int currentWrong = rec.getWrongCount();
        rec.setWrongCount(currentWrong + 1);

        LocalDate next;
        if (currentWrong == 0) {
            next = LocalDate.now().plusDays(7);
        } else {
            next = LocalDate.now().plusDays(3);
        }
        rec.setNextReviewDate(next);
        studyRecordRepository.save(rec);
    }
    
    /**
     * 퀴즈 문제에 좋아요를 누르거나 취소하는 로직 (MyBatis LikePointService 사용)
     */
    @Transactional
    public String toggleLikeStudyRecord(int memberId, int studyRecordId) {
        final String REL_TYPE_CODE = "study_record";

        studyRecordRepository.findById((long)studyRecordId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid study record ID"));

        // 좋아요 상태 확인 및 토글 (MyBatis)
        if (likePointService.getLikePoint(memberId, REL_TYPE_CODE, studyRecordId) != null) {
            likePointService.deleteLikePoint(memberId, REL_TYPE_CODE, studyRecordId);
            return "좋아요 취소"; 
        } else {
            likePointService.insertLikePoint(memberId, REL_TYPE_CODE, studyRecordId);
            return "좋아요 추가";
        }
    }
}