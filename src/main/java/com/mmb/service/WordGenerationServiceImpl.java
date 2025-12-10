package com.mmb.service;

import com.mmb.api.DictionaryApiClient;
import com.mmb.api.DictionaryResponse;
import com.mmb.api.RandomWordProvider;
import com.mmb.api.TranslationClient;
import com.mmb.service.LocalMeaningDictionary;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WordGenerationServiceImpl implements WordGenerationService {

    // 湲곕낯 ?⑥뼱?????濡쒖뺄 ?쒓? ??(踰덉뿭 ?ㅽ뙣 ??蹂댁젙??
    private static final Map<String, String> LOCAL_MEANINGS = LocalMeaningDictionary.MEANINGS;

    private final WordRepository wordRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final TranslationClient translationClient;
    private final RandomWordProvider randomWordProvider;

    @Override
    @Transactional
    public List<Word> generateNewWordsForMember(Member member, int count, Set<Long> excludeWordIds) {
        List<Word> result = new ArrayList<>();
        if (excludeWordIds == null) {
            excludeWordIds = new HashSet<>();
        }
        Set<String> batchSpellings = new HashSet<>();

        int attempts = 0;
        int maxAttempts = Math.max(count * 10, 200);
        while (result.size() < count && attempts < maxAttempts) {
            attempts++;
            String spelling = randomWordProvider.getRandomEnglishWord();
            if (spelling == null || spelling.isBlank()) {
                continue;
            }
            if (batchSpellings.contains(spelling)) {
                continue;
            }

            Optional<Word> existing = wordRepository.findFirstBySpellingIgnoreCase(spelling);
            if (existing.isPresent()) {
                Word w = existing.get();
                if (excludeWordIds.contains(w.getId())) {
                    batchSpellings.add(spelling);
                    continue;
                }
                batchSpellings.add(spelling);
                result.add(w);
                continue;
            }

            DictionaryResponse dict = dictionaryApiClient.lookup(spelling);

            String example = dict != null ? dict.getFirstExampleOrNull() : null;
            String audioUrl = dict != null ? dict.getFirstAudioUrlOrNull() : null;
            String englishDef = dict != null ? dict.getFirstDefinitionOrNull() : null;

            if (englishDef == null || englishDef.isBlank()) {
                englishDef = spelling;
            }

            String meaningKo = translationClient.translateToKorean(englishDef);
            if ((meaningKo == null || meaningKo.isBlank()) && LOCAL_MEANINGS.containsKey(spelling)) {
                meaningKo = LOCAL_MEANINGS.get(spelling);
            }
            if (meaningKo == null || meaningKo.isBlank()) {
                meaningKo = englishDef;
            }
            if (meaningKo == null || meaningKo.isBlank()) {
                meaningKo = spelling;
            }

            Word word = Word.builder()
                    .spelling(spelling)
                    .meaning(meaningKo)
                    .exampleSentence(example)
                    .audioPath(audioUrl)
                    .build();

            wordRepository.save(word);
            batchSpellings.add(spelling);
            result.add(word);
        }

        // ?ъ쟾??遺議깊븯硫?濡쒖뺄 ?ъ쟾?쇰줈 梨꾩썙???붿껌 媛쒖닔瑜?留욎땄
        if (result.size() < count) {
            for (String spelling : LOCAL_MEANINGS.keySet()) {
                if (result.size() >= count) {
                    break;
                }
                if (batchSpellings.contains(spelling)) {
                    continue;
                }
                Optional<Word> existing = wordRepository.findFirstBySpellingIgnoreCase(spelling);
                if (existing.isPresent()) {
                    Word w = existing.get();
                    if (excludeWordIds.contains(w.getId())) {
                        continue;
                    }
                    result.add(w);
                    batchSpellings.add(spelling);
                    continue;
                }
                Word word = Word.builder()
                        .spelling(spelling)
                        .meaning(LOCAL_MEANINGS.get(spelling))
                        .exampleSentence(null)
                        .audioPath(null)
                        .build();
                wordRepository.save(word);
                result.add(word);
                batchSpellings.add(spelling);
            }
        }

        return result;
    }
}


