package com.mmb.service;

import com.mmb.api.DictionaryApiClient;
import com.mmb.api.DictionaryResponse;
import com.mmb.api.RandomWordProvider;
import com.mmb.api.TranslationClient;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordGenerationServiceImpl implements WordGenerationService {

    // 로컬 뜻 사전
    private static final Map<String, String> LOCAL_MEANINGS = LocalMeaningDictionary.MEANINGS;

    private final WordRepository wordRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final TranslationClient translationClient;
    private final RandomWordProvider randomWordProvider;
    private final WordWriteService wordWriteService;

    @Override
    @Transactional(readOnly = true)
    public List<Word> generateNewWordsForMember(Member member, int count, Set<Integer> excludeWordIds) {

        List<Word> result = new ArrayList<>();
        if (count <= 0) return result;

        if (excludeWordIds == null) excludeWordIds = new HashSet<>();
        Set<String> batchSpellings = new HashSet<>();

        int attempts = 0;
        int maxAttempts = Math.max(count * 10, 200);

        while (result.size() < count && attempts < maxAttempts) {
            attempts++;

            String raw = randomWordProvider.getRandomEnglishWord();
            String spelling = normalizeSpelling(raw);
            if (spelling.isBlank()) continue;

            // 같은 배치 내 중복 방지
            if (!batchSpellings.add(spelling)) continue;

            // 1) DB에 이미 있으면 그대로 사용
            Optional<Word> existing = wordRepository.findFirstBySpellingIgnoreCase(spelling);
            if (existing.isPresent()) {
                Word w = existing.get();
                if (w.getId() != null && excludeWordIds.contains(w.getId())) {
                    continue;
                }
                addUniqueById(result, w, count);
                continue;
            }

            // 2) 없으면 API 조회 후 생성
            DictionaryResponse dict = null;
            try {
                dict = dictionaryApiClient.lookup(spelling);
            } catch (Exception e) {
                log.warn("[DICT_FAIL] spelling={}", spelling, e);
            }

            String example = dict != null ? safe(dict.getFirstExampleOrNull()) : "";
            String audioUrl = dict != null ? safe(dict.getFirstAudioUrlOrNull()) : "";
            String englishDef = dict != null ? safe(dict.getFirstDefinitionOrNull()) : "";

            if (englishDef.isBlank()) englishDef = spelling;

            String meaningKo = "";
            try {
                meaningKo = safe(translationClient.translateToKorean(englishDef));
            } catch (Exception e) {
                log.warn("[TRANS_FAIL] spelling={} def={}", spelling, englishDef, e);
            }

            if (meaningKo.isBlank()) {
                meaningKo = safe(LOCAL_MEANINGS.getOrDefault(spelling, ""));
            }
            if (meaningKo.isBlank()) meaningKo = englishDef;

            Word word = Word.builder()
                    .spelling(spelling)
                    .meaning(meaningKo)
                    .exampleSentence(example.isBlank() ? null : example)
                    .audioPath(audioUrl.isBlank() ? null : audioUrl)
                    .build();

            // ✅ 여기서 중복키가 나도 WordWriteService가 "기존 단어"를 반환하게 됨
            Word savedOrExisting = wordWriteService.saveNewWord(word);

            if (savedOrExisting.getId() != null && excludeWordIds.contains(savedOrExisting.getId())) {
                continue;
            }

            addUniqueById(result, savedOrExisting, count);
        }

        // 마지막 fallback(로컬 사전)로 채우기
        if (result.size() < count) {
            for (String spelling : LOCAL_MEANINGS.keySet()) {
                if (result.size() >= count) break;

                String norm = normalizeSpelling(spelling);
                if (norm.isBlank()) continue;
                if (!batchSpellings.add(norm)) continue;

                Optional<Word> existing = wordRepository.findFirstBySpellingIgnoreCase(norm);
                if (existing.isPresent()) {
                    Word w = existing.get();
                    if (w.getId() != null && excludeWordIds.contains(w.getId())) continue;
                    addUniqueById(result, w, count);
                    continue;
                }

                Word w = Word.builder()
                        .spelling(norm)
                        .meaning(LOCAL_MEANINGS.get(spelling))
                        .exampleSentence(null)
                        .audioPath(null)
                        .build();

                Word savedOrExisting = wordWriteService.saveNewWord(w);
                if (savedOrExisting.getId() != null && excludeWordIds.contains(savedOrExisting.getId())) continue;
                addUniqueById(result, savedOrExisting, count);
            }
        }

        return result;
    }

    private void addUniqueById(List<Word> result, Word w, int count) {
        if (w == null || w.getId() == null) return;
        for (Word x : result) {
            if (x != null && x.getId() != null && x.getId().equals(w.getId())) return;
        }
        if (result.size() < count) result.add(w);
    }

    private String normalizeSpelling(String s) {
        if (s == null) return "";
        return s.replace("\r", " ")
                .replace("\n", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\r", " ").replace("\n", " ").trim();
    }
}
