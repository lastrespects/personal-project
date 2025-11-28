package com.mmb.service;

import com.mmb.domain.Word;
import com.mmb.external.client.DictionaryApiClient;
import com.mmb.external.client.TtsClient;
import com.mmb.external.dto.DictionaryApiResponse;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Word 관련 책임 전담 서비스
 * - DB에 없으면 외부사전 조회 -> 예문확보 -> TTS 생성(시도) -> DB 저장
 */
@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final DictionaryApiClient dictionaryApiClient;
    private final TtsClient ttsClient;

    public Word findOrCreate(String spelling) {
        return wordRepository.findBySpelling(spelling).orElseGet(() -> {
            // 1) 사전 조회
            DictionaryApiResponse resp = dictionaryApiClient.fetch(spelling);

            String meaning = "(영문 의미 없음)";
            String example = null;
            if (resp != null && resp.getMeanings() != null && !resp.getMeanings().isEmpty()) {
                var meaningObj = resp.getMeanings().get(0);
                if (meaningObj.getDefinitions() != null && !meaningObj.getDefinitions().isEmpty()) {
                    var def = meaningObj.getDefinitions().get(0);
                    if (def.getDefinition() != null) meaning = def.getDefinition();
                    if (def.getExample() != null && !def.getExample().isBlank()) example = def.getExample();
                }
            }

            // 2) 예문 없으면 간단 생성 (대체 전략; 필요 시 LLM 연동)
            if (example == null || example.isBlank()) {
                example = generateExample(spelling);
            }

            // 3) 엔티티 저장
            Word w = Word.builder()
                    .spelling(spelling)
                    .meaning(meaning)
                    .exampleSentence(example)
                    .audioPath(null)
                    .build();

            Word saved = wordRepository.save(w);

            // 4) TTS 시도 (실패해도 흐름 이어감)
            String audioPath = ttsClient.synthesize(spelling + " — " + example, spelling);
            if (audioPath != null) {
                saved.setAudioPath(audioPath);
                saved = wordRepository.save(saved);
            }

            return saved;
        });
    }

    private String generateExample(String spelling) {
        // 간단한 템플릿 예문. 필요 시 LLM으로 교체
        return "He used the word \"" + spelling + "\" in a sentence.";
    }
}
