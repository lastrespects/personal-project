// src/main/java/com/mmb/service/learning/WordGenerationServiceImpl.java
package com.mmb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmb.api.DictionaryApiClient;
import com.mmb.api.DictionaryResponse;
import com.mmb.api.RandomWordProvider;
import com.mmb.api.TranslationClient;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WordGenerationServiceImpl implements WordGenerationService {

    private final WordRepository wordRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final TranslationClient translationClient;
    private final RandomWordProvider randomWordProvider;

    @Override
    @Transactional
    public List<Word> generateNewWordsForMember(Member member, int count) {
        List<Word> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String spelling = randomWordProvider.getRandomEnglishWord();

            // 1) 이미 있는 단어면 재사용
            Optional<Word> existing = wordRepository.findBySpelling(spelling);
            if (existing.isPresent()) {
                result.add(existing.get());
                continue;
            }

            // 2) DictionaryAPI.dev 호출
            DictionaryResponse dict = dictionaryApiClient.lookup(spelling);

            String example = dict != null ? dict.getFirstExampleOrNull() : null;
            String audioUrl = dict != null ? dict.getFirstAudioUrlOrNull() : null;
            String englishDef = dict != null ? dict.getFirstDefinitionOrNull() : null;

            if (englishDef == null || englishDef.isBlank()) {
                englishDef = spelling; // 정의가 없으면 단어 자체를 번역
            }

            // 3) DeepL로 한국어 뜻 번역
            String meaningKo = translationClient.translateToKorean(englishDef);

            // 4) Word 엔티티 저장
            Word word = Word.builder()
                    .spelling(spelling)
                    .meaning(meaningKo)
                    .exampleSentence(example)
                    .audioPath(audioUrl)
                    .build();

            wordRepository.save(word);
            result.add(word);
        }

        return result;
    }
}
