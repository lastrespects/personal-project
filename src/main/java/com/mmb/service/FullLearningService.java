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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final StudyRecordRepository studyRecordRepository;
    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final WordProgressRepository wordProgressRepository;

    // =========================
    // 1) 단어 학습 기록 처리
    // =========================
    private final WordGenerationService wordGenerationService;

    // =========================
    // 1) 단어 학습 기록 처리
    // =========================
    @Transactional
    public void recordStudy(Long memberId, Long wordId, boolean correct, String studyType) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        StudyRecord record = StudyRecord.builder()
                .memberId(member.getId())
                .wordId(word.getId())
                .studiedAt(now)
                .studyDate(now.toLocalDate())
                .studyType(studyType)
                .correct(correct)
                .build();

        studyRecordRepository.save(record);
    }

    // =========================
    // 2) 오늘 학습한 단어 조회
    // =========================
    public List<Word> getTodayLearnedWords(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return studyRecordRepository.findStudiedWordsBetween(memberId, start, end);
    }

    // =========================
    // 3) 최근 N일 학습 단어
    // =========================
    public List<Word> getLearnedWordsInLastDays(Long memberId, int days) {
        LocalDateTime since = LocalDate.now()
                .minusDays(days)
                .atStartOfDay();

        return studyRecordRepository.findStudiedWordsSince(memberId, since);
    }

    // =========================
    // 4) 최근 학습 단어 Top N
    // =========================
    public List<Word> getRecentLearnedWords(Long memberId, int limit) {
        List<Word> all = studyRecordRepository.findRecentStudiedWords(memberId);
        return all.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    // =========================
    // 5) 오늘의 퀴즈용 출제 단어 선정 (V2 SRS 적용 + AI)
    // =========================
    @Transactional
    public List<Word> buildTodayQuizWordsV2(Long memberId) {
        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElse(null);
        }
        int dailyTarget = member != null && member.getDailyTarget() != null ? member.getDailyTarget() : 30;
        int target = Math.max(5, dailyTarget);

        // A) 오늘 학습한 단어(책/퀴즈 등 전체 학습 기록 기준)
        List<Word> todayLearned = getTodayLearnedWords(memberId);

        // B) 오늘 기준 복습 예정인 오답 단어(SRS 스케줄에 걸린 것들)
        List<Word> dueWrongWords = wordProgressRepository
                .findByMemberIdAndNextReviewDateLessThanEqual(memberId, LocalDate.now())
                .stream()
                .map(WordProgress::getWord)
                .toList();

        // 1. 오늘 학습한 단어를 먼저 담는다 (중복 제거)
        LinkedHashMap<Long, Word> map = new LinkedHashMap<>();
        for (Word w : todayLearned) {
            map.put(w.getId(), w);
        }

        // 2. 복습 대상 단어를 추가 (이미 있는 단어는 제외)
        for (Word w : dueWrongWords) {
            map.putIfAbsent(w.getId(), w);
        }

        // 3. 목표 개수보다 적으면 최근 7일 학습 단어로 보충
        if (map.size() < target) {
            List<Word> last7 = getLearnedWordsInLastDays(memberId, 7);
            for (Word w : last7) {
                if (map.size() >= target)
                    break;
                map.putIfAbsent(w.getId(), w);
            }
        }

        // 4. 그래도 부족하면 AI로 새 단어 생성해서 보충
        if (map.size() < target) {
            int needed = target - map.size();
            Set<Long> excludeWordIds = new HashSet<>(map.keySet());
            List<Word> newWords = wordGenerationService.generateNewWordsForMember(member, needed, excludeWordIds);
            for (Word w : newWords) {
                if (w == null || w.getId() == null) {
                    continue;
                }
                map.putIfAbsent(w.getId(), w);
            }
        }

        return applyDailyShuffle(List.copyOf(map.values()), memberId);
    }


    // =========================
    // 0) 하루 목표량 조회
    // =========================
    public int getDailyTarget(Long memberId) {
        if (memberId == null)
            return 30;
        return memberRepository.findById(memberId)
                .map(Member::getDailyTarget)
                .orElse(30);
    }

    // 오늘 퀴즈 푼 개수
    public long getTodayQuizSolvedCount(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // ★ 여기서 메서드 이름 변경
        return studyRecordRepository.countByMemberIdAndStudyTypeAndStudiedAtBetween(
                memberId, "QUIZ", start, end
        );
    }

    // 오늘 단어장(책)에서 학습한 개수
    public long getTodayBookStudyCount(Long memberId) {
        if (memberId == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // ★ 여기서 메서드 이름 변경
        return studyRecordRepository.countByMemberIdAndStudyTypeAndStudiedAtBetween(
                memberId, "BOOK", start, end
        );
    }

    // 최근 학습 기록 가져오기
    public List<StudyRecord> getRecentStudyRecords(Long memberId, int limit) {
        if (memberId == null) {
            return List.of();
        }

        // ★ 여기서 메서드 이름 변경
        List<StudyRecord> records =
                studyRecordRepository.findTop100ByMemberIdOrderByStudiedAtDesc(memberId);

        if (limit <= 0 || records.size() <= limit) {
            return records;
        }
        return records.subList(0, limit);
    }

    // =========================
    // 6) 퀴즈 결과 반영 + SRS 로직
    // =========================
    @Transactional
    public void applyQuizResult(Long memberId, Long wordId, boolean correct) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        // 1) 학습 로그 저장
        LocalDateTime now = LocalDateTime.now();
        studyRecordRepository.save(StudyRecord.builder()
                .memberId(member.getId())
                .wordId(word.getId())
                .studiedAt(now)
                .studyDate(now.toLocalDate())
                .studyType("QUIZ")
                .correct(correct)
                .build());

        // 2) 진행 상태 업데이트 (WordProgress 갱신)
        WordProgress wp = wordProgressRepository
                .findByMemberIdAndWordId(memberId, wordId)
                .orElseGet(() -> WordProgress.builder()
                        .member(member)
                        .word(word)
                        .wrongStreak(0)
                        .correctCount(0)
                        .wrongCount(0)
                        .build());

        wp.setLastStudiedDate(LocalDate.now());

        if (correct) {
            wp.setCorrectCount(wp.getCorrectCount() + 1);
            wp.setWrongStreak(0);

            // 정답일 때는 복습 간격을 넉넉히(예: 10일 후로 고정)
            wp.setNextReviewDate(LocalDate.now().plusDays(10));
        } else {
            wp.setWrongCount(wp.getWrongCount() + 1);
            wp.setWrongStreak(wp.getWrongStreak() + 1);

            int days = wrongIntervalDays(wp.getWrongStreak());
            wp.setNextReviewDate(LocalDate.now().plusDays(days));
        }

        wordProgressRepository.save(wp);
    }

    // 오답 연속 횟수에 따라 복습 간격 결정
    private int wrongIntervalDays(int wrongStreak) {
        if (wrongStreak <= 1)
            return 7;
        if (wrongStreak == 2)
            return 5;
        if (wrongStreak == 3)
            return 3;
        return 1; // 4회 이상
    }

    // =========================
    // 7) 메인 페이지용 오늘 학습 개수
    // =========================
    public long getTodayLearnedCount(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return studyRecordRepository.countTodayLearnedWords(memberId, start, end);
    }

    private List<Word> applyDailyShuffle(List<Word> words, Long memberId) {
        if (words == null || words.isEmpty()) {
            return words;
        }
        long seed = LocalDate.now().toEpochDay();
        if (memberId != null) {
            seed ^= memberId;
        }
        List<Word> copy = new ArrayList<>(words);
        Collections.shuffle(copy, new Random(seed));
        return copy;
    }
}
