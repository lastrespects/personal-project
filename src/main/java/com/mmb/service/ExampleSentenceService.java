package com.mmb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleSentenceService {

    private final WordRepository wordRepository;
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    // ExampleSentenceService: generates or fetches example sentences for words.
    public String findOrGenerateExample(Word word) {
        if (word == null)
            return "";

        String existing = normalize(word.getExampleSentence());
        if (!existing.isBlank()) {
            return existing;
        }

        String example = fetchFromDictionaryApi(normalize(word.getSpelling()));
        if (example.isBlank()) {
            return "";
        }

        try {
            org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(
                    transactionManager);
            template.setPropagationBehavior(
                    org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            template.execute(status -> {
                word.setExampleSentence(example);
                return wordRepository.save(word);
            });
        } catch (Exception e) {
            log.warn("[EXAMPLE SAVE FAIL] wordId={}, msg={}", word.getId(), e.getMessage());
        }

        return example;
    }

    private String fetchFromDictionaryApi(String spelling) {
        if (spelling.isBlank())
            return "";

        try {
            String encoded = UriUtils.encode(spelling, StandardCharsets.UTF_8);
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + encoded;

            RestTemplate restTemplate = new RestTemplate();
            JsonNode[] body = restTemplate.getForObject(url, JsonNode[].class);
            if (body == null || body.length == 0)
                return "";

            JsonNode meanings = body[0].path("meanings");
            for (JsonNode meaningNode : meanings) {
                JsonNode defs = meaningNode.path("definitions");
                for (JsonNode def : defs) {
                    String ex = normalize(def.path("example").asText(""));
                    if (!ex.isBlank()) {
                        return ex;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[EXAMPLE FETCH ERROR] word={}, msg={}", spelling, e.getMessage());
        }

        return "";
    }

    private String normalize(String value) {
        if (value == null)
            return "";
        String cleaned = value.replace("\r", " ")
                .replace("\n", " ")
                .trim();
        if ("null".equalsIgnoreCase(cleaned))
            return "";
        return cleaned;
    }
}
