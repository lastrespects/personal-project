package com.mmb.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DictionaryApiExampleClient implements ExampleClient {

    @Value("${dictionary.api.base-url:https://api.dictionaryapi.dev/api/v2}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<String> fetchExample(String englishWord) {
        try {
            String url = String.format("%s/entries/en/%s", baseUrl, englishWord);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.isArray() || root.isEmpty()) return Optional.empty();
            for (JsonNode entry : root) {
                JsonNode meanings = entry.path("meanings");
                if (meanings.isArray()) {
                    for (JsonNode meaning : meanings) {
                        JsonNode defs = meaning.path("definitions");
                        if (defs.isArray()) {
                            for (JsonNode def : defs) {
                                String example = def.path("example").asText(null);
                                if (example != null && !example.isBlank()) {
                                    return Optional.of(example.trim());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Dictionary example fetch failed for {}: {}", englishWord, e.getMessage());
        }
        return Optional.empty();
    }
}
