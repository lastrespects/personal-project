package com.mmb.service;

import com.mmb.api.TranslationClient;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.WordRepository;
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

    // ✅ 추가: meaning을 DB에 저장해서 영구 보정하려면 필요
    private final WordRepository wordRepository;

    private final TranslationClient translationClient;
    private final ExampleSentenceService exampleSentenceService;

    @Value("${libre.api.url:https://translate.argosopentech.com/translate}")
    private String libreApiUrl;

    @Value("${libre.api.key:}")
    private String libreApiKey;

    // ✅ 추가 옵션: true면 meaning을 보정했을 때 DB에도 저장
    // application.properties에 없으면 기본 true
    @Value("${learning.meaning.persist:true}")
    private boolean persistMeaningFix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TodayWordDto> prepareTodayWords(Integer memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is required");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        List<Word> words = fullLearningService.ensureTodayWords(member.getId());
        log.info("[WORDBOOK_SERVICE] memberId={} fetchedWords={}", memberId, words == null ? 0 : words.size());
        if (words == null || words.isEmpty()) {
            return List.of();
        }

        // ✅ 핵심: 오늘 단어들에서 meaning이 영어면 한글로 강제 보정(+선택 DB 저장)
        for (Word w : words) {
            ensureKoreanMeaningAndPersistIfNeeded(w);
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

        // ✅ 이제 word.getMeaning() 자체가 보정되어 있을 확률이 높지만,
        // 안전하게 기존 resolveMeaning 로직은 유지
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

    /**
     * ✅ 추가: meaning이 한글이 아니면(영어로 보이면) 한글로 보정해서 word에 세팅
     * - 로컬사전 -> DeepL/Libre 순
     * - persistMeaningFix=true면 DB에도 저장해서 다음부터 계속 한글 뜻 유지
     */
    private void ensureKoreanMeaningAndPersistIfNeeded(Word word) {
        if (word == null) return;

        String spelling = normalize(word.getSpelling());
        if (spelling.isBlank()) return;

        String rawMeaning = normalize(word.getMeaning());

        // 이미 한글이면 OK
        if (containsHangul(rawMeaning)) return;

        // 의미가 비었거나, 영어로 보이거나, 스펠링과 같으면(뜻 실패) 보정 대상
        boolean needFix =
                rawMeaning.isBlank()
                        || looksEnglish(rawMeaning)
                        || rawMeaning.equalsIgnoreCase(spelling);

        if (!needFix) return;

        // 1) 로컬 사전
        String dict = lookupDictionary(spelling);
        if (containsHangul(dict)) {
            applyMeaningFix(word, rawMeaning, dict);
            return;
        }

        // 2) 번역(DeepL -> Libre)
        String translated = safeTranslateToKo(spelling);
        if (containsHangul(translated)) {
            applyMeaningFix(word, rawMeaning, translated);
            return;
        }

        // 보정 실패 시 아무것도 안 함(기존 값 유지)
        log.info("[MEANING FIX FAIL] wordId={}, spelling={}, rawMeaning='{}'", word.getId(), spelling, rawMeaning);
    }

    private void applyMeaningFix(Word word, String before, String after) {
        if (after == null || after.isBlank()) return;

        word.setMeaning(after);

        log.info("[MEANING FIX] wordId={} spelling='{}' before='{}' after='{}'",
                word.getId(), normalize(word.getSpelling()), before, after);

        if (persistMeaningFix) {
            // ✅ 영구 저장(다음부터 MATCH 포함 어디서든 한글 뜻)
            wordRepository.save(word);
        }
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

        // 1) DeepL
        try {
            String deeplResult = normalize(translationClient.translateToKorean(text));
            if (!deeplResult.isBlank()) {
                log.info("[DEEPL OUT] {}", deeplResult);
                return deeplResult;
            }
            log.info("[DEEPL OUT] <blank>");
        } catch (Exception e) {
            log.warn("[DEEPL FAIL] text={}", text, e);
        }

        // 2) LibreTranslate
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

    private boolean looksEnglish(String s) {
        if (s == null) return false;
        // 알파벳이 있고 한글이 없으면 영어로 판단
        return s.matches(".*[A-Za-z].*") && !containsHangul(s);
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
