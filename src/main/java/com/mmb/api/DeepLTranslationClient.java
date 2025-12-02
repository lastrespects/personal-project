// src/main/java/com/mmb/api/DeepLTranslationClient.java
package com.mmb.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeepLTranslationClient implements TranslationClient {

    @Value("${deepl.api.url}")
    private String apiUrl;

    @Value("${deepl.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String translateToKorean(String englishText) {
        // 실제 DeepL 호출 로직은 프로젝트에 맞게 구현하고,
        // 일단은 컴파일을 위해 간단하게 형태만 맞춰둘 수도 있음.

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("text", new String[]{englishText});
            body.put("target_lang", "KO");

            // TODO: DeepL API 스펙에 맞게 request/response 구현
            // Map response = restTemplate.postForObject(apiUrl + "?auth_key=" + apiKey, body, Map.class);
            // 실제 번역 텍스트 꺼내기...

            return englishText; // 임시: 실패 시 원문 반환
        } catch (Exception e) {
            // 에러 시에도 일단 원문 반환
            return englishText;
        }
    }

    // 기존 코드에서 translateEnToKo(...)를 직접 호출하고 있다면,
    // 이렇게 래핑 메서드를 하나 만들어주면 됨 (Override X)
    public String translateEnToKo(String englishText) {
        return translateToKorean(englishText);
    }
}
