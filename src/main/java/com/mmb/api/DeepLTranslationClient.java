package com.mmb.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DeepLTranslationClient implements TranslationClient {

    private final WebClient webClient;

    @Value("${deepl.api.url}")
    private String deeplUrl;

    @Value("${deepl.api.key}")
    private String deeplKey;

    @Override
    public String translateEnToKo(String text) {
        if (text == null || text.isBlank()) return null;

        try {
            Map response = webClient.post()
                    .uri(deeplUrl)
                    .headers(h -> h.set("Authorization", "DeepL-Auth-Key " + deeplKey))
                    .body(BodyInserters
                            .fromFormData("text", text)
                            .with("target_lang", "KO")
                            .with("source_lang", "EN"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return null;

            Object translationsObj = response.get("translations");
            if (!(translationsObj instanceof List<?> translations) || translations.isEmpty())
                return null;

            Object first = translations.get(0);
            if (!(first instanceof Map<?, ?> translationMap))
                return null;

            Object translatedText = translationMap.get("text");
            return translatedText != null ? translatedText.toString() : null;

        } catch (Exception e) {
            System.err.println("[DeepLTranslationClient] 번역 실패: " + e.getMessage());
            return null;
        }
    }
}
