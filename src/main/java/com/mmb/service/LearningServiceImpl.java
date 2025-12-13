package com.mmb.service;

import com.mmb.api.TranslationClient;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningServiceImpl implements LearningService {

    private final FullLearningService fullLearningService;
    private final MemberRepository memberRepository;
    private final TranslationClient translationClient;
    private final ExampleSentenceService exampleSentenceService;

    @Override
    @Transactional(readOnly = true)
    public List<TodayWordDto> prepareTodayWords(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        List<Word> words = fullLearningService.buildTodayQuizWordsV2(member.getId());
        if (words == null || words.isEmpty()) {
            return List.of();
        }

        return words.stream()
                .map(this::convertToDtoSafe)
                .collect(Collectors.toList());
    }

    private TodayWordDto convertToDtoSafe(Word word) {
        if (word == null) {
            return TodayWordDto.builder()
                    .spelling("")
                    .meaning("Meaning pending")
                    .exampleSentence("No example available.")
                    .audioPath("")
                    .review(true)
                    .build();
        }

        String spelling = normalize(word.getSpelling());
        String meaning = resolveMeaning(word, spelling);

        String exampleEn = normalize(exampleSentenceService.findOrGenerateExample(word));

        String exampleDisplay;
        if (exampleEn.isBlank()) {
            exampleDisplay = "No example available.";
        } else {
            String exampleKo = safeTranslateToKo(exampleEn);
            boolean hasKo = containsHangul(exampleKo) && !exampleKo.equalsIgnoreCase(exampleEn);
            if (!hasKo) {
                log.info("[EXAMPLE TRANSLATION MISSING] wordId={}, spelling={}, example='{}', translated='{}'",
                        word.getId(), spelling, exampleEn, exampleKo);
            }
            if (hasKo) {
                exampleDisplay = exampleEn + " / " + exampleKo;
            } else {
                exampleDisplay = exampleEn + " / (번역 준비 중)";
            }
        }

        String audioPath = normalize(word.getAudioPath());

        return TodayWordDto.builder()
                .wordId(word.getId())
                .spelling(spelling)
                .meaning(meaning)
                .exampleSentence(exampleDisplay)
                .audioPath(audioPath)
                .review(true)
                .build();
    }

    private String resolveMeaning(Word word, String spelling) {
        String rawMeaning = normalize(word.getMeaning());
        if (containsHangul(rawMeaning)) {
            return rawMeaning;
        }

        String dict = lookupDictionary(spelling);
        if (containsHangul(dict)) {
            return dict;
        }

        String translated = safeTranslateToKo(spelling);
        if (containsHangul(translated)) {
            return translated;
        }

        if (!rawMeaning.isBlank()) {
            return rawMeaning;
        }
        return "Meaning pending";
    }

    private String lookupDictionary(String spelling) {
        if (spelling == null) return "";
        String value = LocalMeaningDictionary.MEANINGS
                .getOrDefault(spelling.toLowerCase(Locale.ROOT), "");
        return normalize(value);
    }

    private String safeTranslateToKo(String text) {
        if (text == null || text.isBlank()) return "";
        try {
            String translated = translationClient.translateToKorean(text);
            return normalize(translated);
        } catch (Exception e) {
            log.warn("[WORD-BOOK TRANSLATE ERROR] text={}, msg={}", text, e.getMessage());
            return "";
        }
    }

    private boolean containsHangul(String value) {
        return value != null && value.matches(".*[\\u3131-\\u318E\\uAC00-\\uD7A3].*");
    }

    private String normalize(String value) {
        if (value == null) return "";
        String cleaned = value
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
        if ("null".equalsIgnoreCase(cleaned)) return "";
        return cleaned;
    }

    @Override
    @Transactional
    public void recordResult(Long memberId, Long wordId, boolean correct) {
        fullLearningService.applyQuizResult(memberId, wordId, correct);
    }
}
