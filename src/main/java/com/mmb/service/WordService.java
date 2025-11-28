// WordService.java
package com.mmb.service;

import com.mmb.domain.Word; // 패키지 변경
import com.mmb.repository.WordRepository; // 패키지 변경
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    // private final DictionaryApiClient dictionaryApiClient; // 외부 API 클라이언트는 구조에서 제외

    @Transactional
    public Optional<Word> findOrCreate(String spelling) {
        Optional<Word> existing = wordRepository.findBySpelling(spelling);
        if (existing.isPresent()) {
            return existing;
        }

        // TODO: 실제로는 DictionaryApiClient를 사용하여 단어를 가져오는 로직이 필요
        // 현재는 더미 데이터로 대체하거나 API 연동 코드를 추가해야 함.
        Word newWord = Word.builder()
                .spelling(spelling)
                .meaning("더미 의미: " + spelling)
                .exampleSentence("더미 예문: " + spelling + " is a new word.")
                .audioPath("dummy/path")
                .build();
        return Optional.of(wordRepository.save(newWord));
    }
}