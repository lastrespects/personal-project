package com.mmb.service;

import com.mmb.entity.WordProgress;
import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;
import com.mmb.repository.WordProgressRepository;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmb.dto.WordContentDto;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final StudyRecordRepository studyRecordRepository;
    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final WordProgressRepository wordProgressRepository;

    // =========================
    // 1) 단어장 학습 기록 저장
    // =========================
    private final AiContentService aiContentService;
    private final RestTemplate restTemplate = new RestTemplate();

    // =========================
    // 1) 단어장 학습 기록 저장
    // =========================
    @Transactional
    public void recordStudy(Long memberId, Long wordId, boolean correct, String studyType) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        StudyRecord record = StudyRecord.builder()
                .member(member)
                .word(word)
                .studiedAt(LocalDateTime.now())
                .correct(correct)
                .studyType(studyType)
                .build();

        studyRecordRepository.save(record);
    }

    // =========================
    // 2) 오늘 학습 단어
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
    // 4) 최근 학습 단어 top N
    // =========================
    public List<Word> getRecentLearnedWords(Long memberId, int limit) {
        List<Word> all = studyRecordRepository.findRecentStudiedWords(memberId);
        return all.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    // =========================
    // 5) ✅ 오늘의 퀴즈 출제 단어 결정 (V2 SRS 적용 + AI)
    // =========================
    @Transactional
    public List<Word> buildTodayQuizWordsV2(Long memberId) {
        int target = Math.max(5, getDailyTarget(memberId));

        // A) 오늘 단어장에서 배운 단어 (전체 포함)
        List<Word> todayLearned = getTodayLearnedWords(memberId);

        // B) 오늘까지 복습 예정인 오답 단어
        List<Word> dueWrongWords = wordProgressRepository
                .findByMemberIdAndNextReviewDateLessThanEqual(memberId, LocalDate.now())
                .stream()
                .map(WordProgress::getWord)
                .toList();

        // 1. 오늘 배운 단어는 무조건 포함
        LinkedHashMap<Long, Word> map = new LinkedHashMap<>();
        for (Word w : todayLearned) {
            map.put(w.getId(), w);
        }

        // 2. 복습 단어 추가 (이미 포함된 단어 제외)
        for (Word w : dueWrongWords) {
            map.putIfAbsent(w.getId(), w);
        }

        // 3. 목표 개수보다 부족하면 최근 학습 단어로 보충
        if (map.size() < target) {
            List<Word> last7 = getLearnedWordsInLastDays(memberId, 7);
            for (Word w : last7) {
                if (map.size() >= target)
                    break;
                map.putIfAbsent(w.getId(), w);
            }
        }

        // 4. 그래도 부족하면 AI로 새 단어 생성해서 추가!
        if (map.size() < target) {
            int needed = target - map.size();
            List<Word> newWords = fetchAndGenerateNewWords(needed);
            for (Word w : newWords) {
                map.put(w.getId(), w);
            }
        }

        return List.copyOf(map.values());
    }

    private List<Word> fetchAndGenerateNewWords(int count) {
        List<Word> generatedWords = new java.util.ArrayList<>();
        try {
            // 1. Random Word API에서 단어 가져오기
            String randomWordUrl = "https://random-word-api.herokuapp.com/word?number=" + count;
            String[] words = restTemplate.getForObject(randomWordUrl, String[].class);

            if (words != null) {
                for (String w : words) {
                    // 2. 이미 DB에 있는지 확인
                    if (wordRepository.findBySpelling(w).isPresent()) {
                        generatedWords.add(wordRepository.findBySpelling(w).get());
                        continue;
                    }

                    // 3. AI로 뜻과 예문 생성
                    WordContentDto content = aiContentService.generateWordContent(w);

                    // 4. DB 저장
                    Word newWord = Word.builder()
                            .spelling(w)
                            .meaning(content.getMeaning())
                            .exampleSentence(content.getExampleSentence())
                            .audioPath(null) // TTS는 나중에 처리하거나 null
                            .build();

                    wordRepository.save(newWord);
                    generatedWords.add(newWord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 로그 남기기
        }
        return generatedWords;
    }

    // =========================
    // 0) 목표량 조회
    // =========================
    public int getDailyTarget(Long memberId) {
        if (memberId == null)
            return 30;
        return memberRepository.findById(memberId)
                .map(Member::getDailyTarget)
                .orElse(30);
    }

    // =========================
    // 6) 퀴즈 결과 반영 + SRS 로직
    // =========================
    @Transactional
    public void applyQuizResult(Long memberId, Long wordId, boolean correct) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();

        // 1) 로그 저장
        studyRecordRepository.save(StudyRecord.builder()
                .member(member)
                .word(word)
                .studiedAt(LocalDateTime.now())
                .correct(correct)
                .studyType("QUIZ")
                .build());

        // 2) 진행 상태 갱신
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

            // ✅ 정답일 때는 복습을 10일 뒤로 미룸 (단순화)
            wp.setNextReviewDate(LocalDate.now().plusDays(10));
        } else {
            wp.setWrongCount(wp.getWrongCount() + 1);
            wp.setWrongStreak(wp.getWrongStreak() + 1);

            int days = wrongIntervalDays(wp.getWrongStreak());
            wp.setNextReviewDate(LocalDate.now().plusDays(days));
        }

        wordProgressRepository.save(wp);
    }

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
    // 7) 메인 페이지용 오늘 학습 카운트
    // =========================
    public long getTodayLearnedCount(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return studyRecordRepository.countTodayLearnedWords(memberId, start, end);
    }
}
