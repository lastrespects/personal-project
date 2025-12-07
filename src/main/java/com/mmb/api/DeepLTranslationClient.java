// src/main/java/com/mmb/api/DeepLTranslationClient.java
package com.mmb.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("auth_key", apiKey);
            body.add("text", englishText);
            body.add("target_lang", "KO");
            body.add("source_lang", "EN");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(apiUrl, request, String.class);
            if (response == null || response.isBlank()) {
                return englishText;
            }
            JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
            JsonNode translations = root.path("translations");
            if (translations.isArray() && translations.size() > 0) {
                String text = translations.get(0).path("text").asText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        } catch (Exception ignored) {
        }
        return englishText;
    }

    // 기존 헬퍼
    public String translateEnToKo(String englishText) {
        return translateToKorean(englishText);
    }
}
