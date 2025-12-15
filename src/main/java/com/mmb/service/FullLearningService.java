package com.mmb.service;

import com.mmb.entity.DailyWordItem;
import com.mmb.entity.DailyWordSet;
import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import com.mmb.entity.WordProgress;
import com.mmb.repository.DailyWordItemRepository;
import com.mmb.repository.DailyWordSetRepository;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;
import com.mmb.repository.WordProgressRepository;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullLearningService {

    private final StudyRecordRepository studyRecordRepository;
    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final WordProgressRepository wordProgressRepository;

    private final DailyWordSetRepository dailyWordSetRepository;
    private final DailyWordItemRepository dailyWordItemRepository;

    private final LearningWriteService learningWriteService;
    private final WordGenerationService wordGenerationService;

    // =========================================================
    // 0) “오늘 고정 세트” 기반으로 단어장/퀴즈 모두 동일하게 사용
    // =========================================================
    // =========================================================
    // 0) “오늘 고정 세트” 기반으로 단어장/퀴즈 모두 동일하게 사용
    // =========================================================
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Word> ensureTodayWords(Integer memberId) {
        if (memberId == null)
            return List.of();

        LocalDate today = LocalDate.now();
        // 3) 목표량 변경 직후 즉시 반영 보장: 매 요청마다 DB에서 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        int currentTarget = (member.getDailyTarget() != null) ? member.getDailyTarget() : 30;
        if (currentTarget < 0)
            currentTarget = 0;

        // 1) 오늘 세트가 있는지 확인
        Optional<DailyWordSet> setOpt = dailyWordSetRepository.findByMemberIdAndStudyDate(memberId, today);
        List<Word> words;
        int beforeCount = 0;

        if (setOpt.isEmpty()) {
            // 아예 없으면 새로 생성
            log.info("[DWS_CREATE] memberId={} date={} target={}", memberId, today, currentTarget);
            words = createTodaySetWordsTx(memberId, today);
            beforeCount = 0;
        } else {
            // 2) 있으면 가져오기
            DailyWordSet set = setOpt.get();
            words = loadWordsFromSet(set.getId());
            beforeCount = words.size();

            // 3) Refill: 개수 부족하면 채우기 (사용자가 목표를 늘렸을 경우)
            if (words.size() < currentTarget) {
                int need = currentTarget - words.size();
                log.info("[DWS_REFILL] memberId={} exist={} target={} need={}", memberId, words.size(), currentTarget,
                        need);

                try {
                    // 기존 단어 ID 제외
                    Set<Integer> exclude = words.stream().map(Word::getId).collect(Collectors.toSet());
                    List<Word> newWords = wordGenerationService.generateNewWordsForMember(member, need, exclude);

                    if (!newWords.isEmpty()) {
                        LocalDateTime now = LocalDateTime.now();
                        List<DailyWordItem> newItems = new ArrayList<>();
                        int nextOrder = words.size() + 1;

                        for (Word w : newWords) {
                            if (w.getId() == null)
                                continue;
                            newItems.add(DailyWordItem.builder()
                                    .setId(set.getId())
                                    .wordId(w.getId())
                                    .sourceCode("REFILL")
                                    .sortOrder(nextOrder++)
                                    .regDate(now)
                                    .build());
                            words.add(w); // 리스트에도 추가
                        }
                        dailyWordItemRepository.saveAll(newItems);

                        // 세트 정보 업데이트
                        set.setTargetCount(currentTarget); // 타겟 업데이트 반영
                        set.setUpdateDate(now);
                        dailyWordSetRepository.save(set);
                    }
                } catch (Exception e) {
                    log.error("[DWS_REFILL_FAIL] Failed to generate/save words", e);
                    // 실패해도 기존 단어라도 반환
                }
            }
        }

        // 4) Truncate: list.size() > target 이면: DB 삭제하지 말고 "응답에서만" target 만큼만 잘라서 반환
        int afterCount = words.size();
        List<Word> finalResult = words;
        if (words.size() > currentTarget) {
            finalResult = new ArrayList<>(words.subList(0, currentTarget));
        }

        log.info("[ENSURE_TODAY] username={} target={} before={} after={} returned={}",
                member.getUsername(), currentTarget, beforeCount, afterCount, finalResult.size());

        return finalResult;
    }

    // Deprecated adapter for backward compatibility if needed, or just removed if
    // all callers updated.
    // We will update callers to use ensureTodayWords.

    private List<Word> loadWordsFromSet(Integer setId) {
        List<DailyWordItem> items = dailyWordItemRepository.findBySetIdOrderBySortOrderAsc(setId);
        if (items == null || items.isEmpty())
            return List.of();

        List<Integer> wordIds = items.stream().map(DailyWordItem::getWordId).toList();
        Map<Integer, Word> map = wordRepository.findAllById(wordIds).stream()
                .collect(Collectors.toMap(Word::getId, w -> w));

        List<Word> ordered = new ArrayList<>();
        for (Integer wid : wordIds) {
            Word w = map.get(wid);
            if (w != null)
                ordered.add(w);
        }
        return ordered;
    }

    // ✅ 세트 생성은 “쓰기 트랜잭션”으로 강제
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Word> createTodaySetWordsTx(Integer memberId, LocalDate today) {
        if (memberId == null)
            return List.of();

        // 동시성 대비: 안에서 한번 더 조회
        // 동시성 대비: 안에서 한번 더 조회
        Optional<DailyWordSet> existing = dailyWordSetRepository.findByMemberIdAndStudyDate(memberId, today);
        if (existing.isPresent()) {
            return loadWordsFromSet(existing.get().getId());
        }

        Member member = memberRepository.findById(memberId).orElse(null);
        int targetCount = (member != null && member.getDailyTarget() != null) ? member.getDailyTarget() : 30;
        if (targetCount < 0)
            targetCount = 0;

        log.info("[DWS_BUILD] memberId={} date={} targetCount={}", memberId, today, targetCount);

        // targetCount가 0이면 빈 세트를 저장해두고(고정) 그대로 반환
        DailyWordSet set = DailyWordSet.builder()
                .memberId(memberId)
                .studyDate(today)
                .targetCount(targetCount)
                .regDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
        DailyWordSet savedSet = dailyWordSetRepository.save(set);

        if (targetCount == 0) {
            return List.of();
        }

        // 1) 오늘 학습 단어
        List<Word> todayLearned = getTodayLearnedWords(memberId);

        // 2) 복습(틀린단어/오답 누적) 대상: nextReviewDate <= today
        List<Word> dueWrongWords = wordProgressRepository
                .findByMemberIdAndNextReviewDateLessThanEqual(memberId, today)
                .stream()
                .map(WordProgress::getWord)
                .filter(Objects::nonNull)
                .toList();

        // 3) 최근 7일 학습 단어
        List<Word> last7 = getLearnedWordsInLastDays(memberId, 7);

        LinkedHashMap<Integer, Pick> picked = new LinkedHashMap<>();

        // 우선순위: TODAY -> REVIEW(틀린단어) -> LAST7 -> GENERATED
        addWords(picked, todayLearned, "TODAY", targetCount);
        addWords(picked, dueWrongWords, "REVIEW", targetCount);
        addWords(picked, last7, "LAST7", targetCount);

        int generated = 0;
        if (picked.size() < targetCount) {
            int need = targetCount - picked.size();
            Set<Integer> excludeWordIds = new HashSet<>(picked.keySet());
            List<Word> newWords = wordGenerationService.generateNewWordsForMember(member, need, excludeWordIds);
            if (newWords != null) {
                generated = newWords.size();
                addWords(picked, newWords, "GENERATED", targetCount);
            }
        }

        // 셔플은 “생성 시 1회만” 하고 sortOrder로 고정
        List<Pick> finalList = new ArrayList<>(picked.values());
        deterministicShuffle(finalList, memberId, today);

        // daily_word_item 저장
        LocalDateTime now = LocalDateTime.now();
        List<DailyWordItem> items = new ArrayList<>();
        int order = 1;
        for (Pick p : finalList) {
            if (p.word == null || p.word.getId() == null)
                continue;
            items.add(DailyWordItem.builder()
                    .setId(savedSet.getId())
                    .wordId(p.word.getId())
                    .sourceCode(p.sourceCode)
                    .sortOrder(order++)
                    .regDate(now)
                    .build());
        }
        dailyWordItemRepository.saveAll(items);

        List<Word> result = finalList.stream()
                .map(x -> x.word)
                .filter(Objects::nonNull)
                .toList();

        log.info("[DWS_DONE] memberId={} date={} setId={} result={} generated={}",
                memberId, today, savedSet.getId(), result.size(), generated);

        return result;
    }

    private void addWords(LinkedHashMap<Integer, Pick> picked, List<Word> words, String source, int limit) {
        if (words == null)
            return;
        for (Word w : words) {
            if (picked.size() >= limit)
                return;
            if (w == null || w.getId() == null)
                continue;
            picked.putIfAbsent(w.getId(), new Pick(w, source));
        }
    }

    private void deterministicShuffle(List<Pick> list, Integer memberId, LocalDate date) {
        if (list == null || list.size() <= 1)
            return;
        long seed = date.toEpochDay();
        if (memberId != null)
            seed ^= memberId;
        Collections.shuffle(list, new Random(seed));
    }

    private static class Pick {
        final Word word;
        final String sourceCode;

        Pick(Word word, String sourceCode) {
            this.word = word;
            this.sourceCode = sourceCode;
        }
    }

    // =========================================================
    // 1) 학습 기록 처리 (쓰기)
    // =========================================================
    @Transactional
    public void recordStudy(Integer memberId, Integer wordId, boolean correct, String studyType) {
        learningWriteService.recordStudy(memberId, wordId, correct, studyType);
    }

    // =========================================================
    // 2) 오늘 학습한 단어 조회
    // =========================================================
    @Transactional(readOnly = true)
    public List<Word> getTodayLearnedWords(Integer memberId) {
        if (memberId == null)
            return List.of();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return studyRecordRepository.findStudiedWordsBetween(memberId, start, end);
    }

    // ✅ 메인에서 쓰는 메서드: (에러 해결용)
    @Transactional(readOnly = true)
    public long getTodayLearnedCount(Integer memberId) {
        if (memberId == null)
            return 0;
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        // 너 레포지토리에 이미 있는 메서드 쓰는 방식(기존 코드 유지)
        return studyRecordRepository.countTodayLearnedWords(memberId, start, end);
    }

    // =========================================================
    // 3) 최근 N일 학습 단어
    // =========================================================
    @Transactional(readOnly = true)
    public List<Word> getLearnedWordsInLastDays(Integer memberId, int days) {
        if (memberId == null)
            return List.of();
        LocalDateTime since = LocalDate.now().minusDays(days).atStartOfDay();
        return studyRecordRepository.findStudiedWordsSince(memberId, since);
    }

    // =========================================================
    // 4) 최근 학습 단어 Top N
    // =========================================================
    @Transactional(readOnly = true)
    public List<Word> getRecentLearnedWords(Integer memberId, int limit) {
        if (memberId == null)
            return List.of();
        List<Word> all = studyRecordRepository.findRecentStudiedWords(memberId);
        return all.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    // =========================================================
    // 5) 하루 목표량 조회
    // =========================================================
    @Transactional(readOnly = true)
    public int getDailyTarget(Integer memberId) {
        if (memberId == null)
            return 30;
        return memberRepository.findById(memberId)
                .map(Member::getDailyTarget)
                .orElse(30);
    }

    // =========================================================
    // 6) 퀴즈 결과 반영 (쓰기)
    // =========================================================
    @Transactional
    public void applyQuizResult(Integer memberId, Integer wordId, boolean correct) {
        learningWriteService.applyQuizResult(memberId, wordId, correct);
    }

    // =========================================================
    // 7) 오늘 퀴즈 푼 개수
    // =========================================================
    @Transactional(readOnly = true)
    public long getTodayQuizSolvedCount(Integer memberId) {
        if (memberId == null)
            return 0;
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return studyRecordRepository.countByMemberIdAndStudyTypeAndStudiedAtBetween(memberId, "QUIZ", start, end);
    }

    // =========================================================
    // 7-1) 오늘 퀴즈 정답 개수
    // =========================================================
    @Transactional(readOnly = true)
    public long getTodayQuizCorrectCount(Integer memberId) {
        if (memberId == null)
            return 0;

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return studyRecordRepository.countQuizCorrect(memberId, start, end);
    }

    // =========================================================
    // 8) 오늘 단어장(BOOK) 학습 수
    // =========================================================
    @Transactional(readOnly = true)
    public long getTodayBookStudyCount(Integer memberId) {
        if (memberId == null)
            return 0;

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return studyRecordRepository.countByMemberIdAndStudyTypeAndStudiedAtBetween(memberId, "BOOK", start, end);
    }

    // =========================================================
    // 9) 최근 학습 기록
    // =========================================================
    @Transactional(readOnly = true)
    public List<StudyRecord> getRecentStudyRecords(Integer memberId, int limit) {
        if (memberId == null)
            return List.of();
        List<StudyRecord> records = studyRecordRepository.findTop100ByMemberIdOrderByStudiedAtDesc(memberId);
        if (limit <= 0 || records.size() <= limit)
            return records;
        return records.subList(0, limit);
    }
}
