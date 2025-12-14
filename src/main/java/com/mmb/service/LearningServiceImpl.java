package com.mmb.service;

import com.mmb.api.TranslationClient;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningServiceImpl implements LearningService {

    private final FullLearningService fullLearningService;
    private final MemberRepository memberRepository;
    private final TranslationClient translationClient;
    private final ExampleSentenceService exampleSentenceService;

    @Value("${libre.api.url:https://translate.argosopentech.com/translate}")
    private String libreApiUrl;

    @Value("${libre.api.key:}")
    private String libreApiKey;

    @Override
    @Transactional(readOnly = true)
    public List<TodayWordDto> prepareTodayWords(Integer memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is required");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        List<Word> words = fullLearningService.buildTodayQuizWordsV2(member.getId());
        log.info("[WORDBOOK_SERVICE] memberId={} fetchedWords={}", memberId, words == null ? 0 : words.size());
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
            if (!exampleKo.isBlank() && !exampleKo.equalsIgnoreCase(exampleEn)) {
                exampleDisplay = exampleEn + " / " + exampleKo;
            } else {
                log.info("[EXAMPLE TRANSLATION MISSING] wordId={}, spelling={}, example='{}', exampleKo='{}'",
                        word.getId(), spelling, exampleEn, exampleKo);
                exampleDisplay = exampleEn + " / (번역 실패)";
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
        if (text == null || text.isBlank()) {
            return "";
        }

        log.info("[TRANSLATE IN] {}", text);
        String deeplResult = "";
        try {
            deeplResult = normalize(translationClient.translateToKorean(text));
            if (!deeplResult.isBlank()) {
                log.info("[DEEPL OUT] {}", deeplResult);
                return deeplResult;
            }
            log.info("[DEEPL OUT] <blank>");
        } catch (Exception e) {
            log.warn("[DEEPL FAIL] text={}", text, e);
        }

        String libreResult = translateWithLibre(text);
        if (!libreResult.isBlank()) {
            log.info("[LIBRE OUT] {}", libreResult);
            return libreResult;
        }
        log.warn("[LIBRE OUT] <blank>");
        return "";
    }

    private String translateWithLibre(String text) {
        if (libreApiUrl == null || libreApiUrl.isBlank()) {
            log.warn("[LIBRE SKIP] URL not configured");
            return "";
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put("q", text);
            body.put("source", "en");
            body.put("target", "ko");
            body.put("format", "text");
            if (libreApiKey != null && !libreApiKey.isBlank()) {
                body.put("api_key", libreApiKey);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            var responseEntity = restTemplate.postForEntity(libreApiUrl, entity, Map.class);
            log.info("[LIBRE STATUS] {}", responseEntity.getStatusCode());
            log.info("[LIBRE BODY] {}", responseEntity.getBody());
            Map<?, ?> response = responseEntity.getBody();
            if (response != null) {
                Object translated = response.get("translatedText");
                if (translated instanceof String translatedText) {
                    return normalize(translatedText);
                }
            }
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.warn("[LIBRE FAIL] status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.warn("[LIBRE FAIL] text={}", text, e);
        }
        return "";
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
    @Transactional(readOnly = false)
    public void recordResult(Integer memberId, Integer wordId, boolean correct) {
        fullLearningService.applyQuizResult(memberId, wordId, correct);
    }
}
