package com.mmb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class AiContentService {

    /**
     * 예문/간단 문장을 만들어 달라는 프롬프트를 받고
     * 아주 단순한 영어 문장 하나를 돌려주는 더미 메서드입니다.
     *
     * 나중에 진짜 OpenAI, HuggingFace 같은 API를 붙일 때
     * 이 메서드 안의 구현만 교체하면 됩니다.
     */
    public String generateSimpleText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "";
        }

        // 프롬프트 안에 '단어'가 작은따옴표('word')로 들어오는 경우 추출
        String keyword = extractWordFromPrompt(prompt);

        if (keyword == null || keyword.isBlank()) {
            // 단어를 못 찾으면 그냥 안전한 기본 문장
            return "This is an example sentence.";
        }

        // 아주 단순한 예문 생성 (영어 한 문장만)
        // -> ExampleSentenceService가 여기 결과를 받아서
        //    번역 클라이언트(TranslationClient)로 한국어 해석을 붙일 거야.
        return "This is an example sentence using the word \"" + keyword + "\".";
    }

    /**
     * 프롬프트 안에서 작은따옴표로 감싸진 단어를 추출합니다.
     * 예: "단어 'answer'에 대한 예문"  ->  answer
     */
    private String extractWordFromPrompt(String prompt) {
        try {
            int first = prompt.indexOf('\'');
            if (first == -1) return null;

            int second = prompt.indexOf('\'', first + 1);
            if (second == -1) return null;

            String w = prompt.substring(first + 1, second).trim();
            if (w.isEmpty()) return null;

            return w.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            log.warn("[AI-CONTENT] failed to extract word from prompt: {}, msg={}", prompt, e.getMessage());
            return null;
        }
    }
}
