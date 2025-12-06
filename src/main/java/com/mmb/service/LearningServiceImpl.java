package com.mmb.service;

import com.mmb.api.TranslationClient;
import com.mmb.service.LocalMeaningDictionary;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {
    private static final Map<String, String> LOCAL_MEANINGS = LocalMeaningDictionary.MEANINGS;
    private final MemberRepository memberRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final WordGenerationService wordGenerationService;
    private final TranslationClient translationClient;
    private final WordRepository wordRepository;

    @Override
    @Transactional
    public List<TodayWordDto> prepareTodayWords(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("?뚯썝??議댁옱?섏? ?딆뒿?덈떎. ID=" + memberId));

        int targetCount = Optional.ofNullable(member.getDailyTarget()).orElse(30);

        // 1) ?대떦 ?뚯썝??紐⑤뱺 ?숈뒿 湲곕줉 議고쉶
        List<StudyRecord> allRecords = studyRecordRepository.findByMemberId(memberId);
        Set<Long> existingWordIds = allRecords.stream()
                .filter(r -> r.getWord() != null)
                .map(r -> r.getWord().getId())
                .collect(Collectors.toSet());

        // 2) ?ㅻ뒛 蹂듭뒿?댁빞 ???⑥뼱 異붿텧 (SRS)
        List<StudyRecord> dueRecords = allRecords.stream()
                .filter(this::isDueForReview)
                .sorted(this::comparePriority)
                .limit(targetCount)
                .toList();

        LinkedHashMap<Long, TodayWordDto> uniqMap = new LinkedHashMap<>();

        for (StudyRecord record : dueRecords) {
            Word w = record.getWord();
            String meaning = resolveMeaning(w);
            uniqMap.put(w.getId(), TodayWordDto.builder()
                    .wordId(w.getId())
                    .spelling(w.getSpelling())
                    .meaning(meaning)
                    .exampleSentence(w.getExampleSentence())
                    .audioPath(w.getAudioPath())
                    .review(true)
                    .build());
        }

        int remain = targetCount - uniqMap.size();

        // 3) 遺議깊븯硫????⑥뼱 ?앹꽦
        if (remain > 0) {
            List<Word> newWords = wordGenerationService.generateNewWordsForMember(member, remain, existingWordIds);
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

                String meaning = resolveMeaning(w);
                uniqMap.put(w.getId(), TodayWordDto.builder()
                        .wordId(w.getId())
                        .spelling(w.getSpelling())
                        .meaning(meaning)
                        .exampleSentence(w.getExampleSentence())
                        .audioPath(w.getAudioPath())
                        .review(false)
                        .build());
            }
        }

        // ?꾩슂 ??異붽? ?앹꽦?쇰줈 梨꾩슦湲?(以묐났 ?쒓굅 ??遺議깅텇 蹂댁셿)
        int stillNeed = targetCount - uniqMap.size();
        if (stillNeed > 0) {
            Set<Long> exclude = uniqMap.keySet();
            List<Word> extra = wordGenerationService.generateNewWordsForMember(member, stillNeed, exclude);
            for (Word w : extra) {
                if (uniqMap.containsKey(w.getId())) continue;
                StudyRecord record = StudyRecord.builder()
                        .member(member)
                        .word(w)
                        .correctCount(0)
                        .incorrectCount(0)
                        .totalAttempts(0)
                        .lastReviewDate(null)
                        .build();
                studyRecordRepository.save(record);
                uniqMap.put(w.getId(), TodayWordDto.builder()
                        .wordId(w.getId())
                        .spelling(w.getSpelling())
                        .meaning(resolveMeaning(w))
                        .exampleSentence(w.getExampleSentence())
                        .audioPath(w.getAudioPath())
                        .review(false)
                        .build());
                if (uniqMap.size() >= targetCount) break;
            }
        }

        return new java.util.ArrayList<>(uniqMap.values());
    }

    @Override
    @Transactional
    public void recordResult(Long memberId, Long wordId, boolean correct) {
        StudyRecord record = studyRecordRepository
                .findByMemberIdAndWordId(memberId, wordId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "?숈뒿 湲곕줉???놁뒿?덈떎. memberId=" + memberId + ", wordId=" + wordId));

        record.setTotalAttempts(record.getTotalAttempts() + 1);
        if (correct) {
            record.setCorrectCount(record.getCorrectCount() + 1);
        } else {
            record.setIncorrectCount(record.getIncorrectCount() + 1);
        }
        record.setLastReviewDate(LocalDateTime.now());
    }

    // =============================
    // SRS 愿???대? 濡쒖쭅
    // =============================

    /**
     * ???⑥뼱媛 ?ㅻ뒛 蹂듭뒿 ??곸씤吏 ?щ?
     */
    private boolean isDueForReview(StudyRecord record) {
        if (record.getLastReviewDate() == null || record.getTotalAttempts() == 0) {
            return false; // ?꾩쭅 蹂????녿뒗 ?⑥뼱??"蹂듭뒿 ??? ?꾨떂
        }

        int correct = record.getCorrectCount();
        int incorrect = record.getIncorrectCount();
        int score = correct - incorrect;

        int intervalDays;
        if (score <= 0) {
            intervalDays = 1; // ?대젮???⑥뼱: ?섎（留덈떎
        } else if (score <= 2) {
            intervalDays = 2;
        } else if (score <= 4) {
            intervalDays = 4;
        } else {
            intervalDays = 7; // 異⑸텇???듯엺 ?⑥뼱: 7?쇰쭏??
        }

        LocalDateTime nextReviewDate = record.getLastReviewDate().plusDays(intervalDays);
        return !nextReviewDate.isAfter(LocalDateTime.now());
    }

    /**
     * ?대뼡 ?⑥뼱瑜?癒쇱? 蹂댁뿬以꾩? ?뺣젹 湲곗?
     */
    private int comparePriority(StudyRecord a, StudyRecord b) {
        int scoreA = a.getCorrectCount() - a.getIncorrectCount();
        int scoreB = b.getCorrectCount() - b.getIncorrectCount();

        if (scoreA != scoreB) {
            return Integer.compare(scoreA, scoreB); // ?먯닔 ???(?섎뱺) ?⑥뼱 癒쇱?
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

    private String resolveMeaning(Word w) {
        String meaning = (w.getMeaning() != null && !w.getMeaning().isBlank()) ? w.getMeaning() : null;
        String spellingLower = w.getSpelling() != null ? w.getSpelling().toLowerCase() : "";

        // 이미 값이 있지만 스펠링과 같거나 영문만 있으면 한글로 교체 시도
        if (meaning != null) {
            String meaningLower = meaning.toLowerCase();
            boolean looksEnglishOnly = meaning.matches("(?i)[a-z\\s]+");
            if (!meaningLower.equals(spellingLower) && !looksEnglishOnly) {
                return meaning; // 이미 한글처럼 보이면 그대로 사용
            }
        }

        String local = LOCAL_MEANINGS.get(spellingLower);
        if (local != null && !local.isBlank()) {
            w.setMeaning(local);
            wordRepository.save(w);
            return local;
        }
        try {
            String translated = translationClient.translateToKorean(w.getSpelling());
            if (translated != null && !translated.isBlank()) {
                w.setMeaning(translated);
                wordRepository.save(w);
                return translated;
            }
        } catch (Exception ignored) {
        }
        return w.getSpelling();
    }
}





