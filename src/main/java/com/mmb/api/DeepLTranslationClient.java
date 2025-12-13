package com.mmb.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DeepLTranslationClient implements TranslationClient {

    @Value("${deepl.api.url:}")
    private String apiUrl;

    @Value("${deepl.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);
        return new RestTemplate(factory);
    }

    @Override
    public String translateToKorean(String englishText) {
        if (englishText == null || englishText.isBlank()) {
            return englishText == null ? "" : englishText;
        }

        if (apiUrl == null || apiUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            return englishText;
        }

        try {
            RestTemplate restTemplate = buildRestTemplate();

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

            JsonNode translations = objectMapper.readTree(response).path("translations");
            if (translations.isArray() && translations.size() > 0) {
                String text = translations.get(0).path("text").asText();
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        } catch (Exception e) {
            log.warn("[DEEPL ERROR] {}", e.getMessage());
        }

        return englishText;
    }
}