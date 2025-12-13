package com.mmb.service;

import com.mmb.api.DictionaryApiClient;
import com.mmb.api.DictionaryApiClient.DictionaryWordInfo;
import com.mmb.api.TranslationClient;
import com.mmb.api.RandomWordClient;
import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordService {

    private final WordRepository wordRepository;
    private final RandomWordClient randomWordClient;
    private final DictionaryApiClient dictionaryApiClient;
    private final TranslationClient translationClient;

    @PostConstruct
    @Transactional
    public void initTestData() {
        if (wordRepository.count() == 0) {
            fetchAndSaveMockWords(30);
        }
    }

    @Transactional
    public void fetchAndSaveMockWords(int count) {
        List<String> randomWords = randomWordClient.getRandomWords(count);
        if (randomWords.isEmpty()) {
            System.out.println("[WordService] 랜덤 단어를 가져오지 못했습니다.");
            return;
        }

        for (String rawWord : randomWords) {
            String w = rawWord.trim();
            if (w.isEmpty())
                continue;

            if (wordRepository.existsBySpelling(w)) {
                continue;
            }

            DictionaryWordInfo info = dictionaryApiClient.fetchWordInfo(w);
            if (info == null)
                continue;

            String englishDef = info.getDefinition();
            String korean = translationClient.translateToKorean(englishDef);

            Word entity = Word.builder()
                    .spelling(info.getSpelling())
                    .meaning(korean)
                    .exampleSentence(info.getExample())
                    .audioPath(info.getAudioUrl())
                    .build();

            wordRepository.save(entity);
        }
    }

    public List<Word> findAllWords() {
        return wordRepository.findAll();
    }
}
