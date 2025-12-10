package com.mmb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmb.dto.WordContentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiContentService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WordContentDto generateWordContent(String word) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API Key is missing. Returning fallback content.");
            return WordContentDto.builder()
                    .word(word)
                    .meaning("API Key Missing")
                    .exampleSentence("Please configure your Gemini API Key.")
                    .build();
        }

        try {
            // Construct the prompt
            String promptText = String.format(
                    "Provide the Korean meaning and a simple English example sentence for the word '%s'. " +
                            "Return ONLY a JSON object with keys: 'meaning' (Korean string) and 'example' (English string). "
                            +
                            "Do not include markdown formatting like ```json.",
                    word);

            // Build Request Body for Gemini
            Map<String, Object> contentPart = new HashMap<>();
            contentPart.put("text", promptText);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(contentPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Add API Key as query param or header (Gemini usually takes it as query param
            // ?key=...)
            // But for safety, let's append it to the URL if not present
            String finalUrl = geminiApiUrl;
            if (!finalUrl.contains("key=")) {
                finalUrl += "?key=" + geminiApiKey;
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Execute Request
            String response = restTemplate.postForObject(finalUrl, entity, String.class);

            // Parse Response
            JsonNode root = objectMapper.readTree(response);
            String responseText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text")
                    .asText();

            // Clean up potential markdown code blocks if the model ignores the instruction
            responseText = responseText.replace("```json", "").replace("```", "").trim();

            JsonNode jsonResult = objectMapper.readTree(responseText);

            return WordContentDto.builder()
                    .word(word)
                    .meaning(jsonResult.path("meaning").asText())
                    .exampleSentence(jsonResult.path("example").asText())
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate content for word: {}", word, e);
            return WordContentDto.builder()
                    .word(word)
                    .meaning("Generation Failed")
                    .exampleSentence("Could not generate example.")
                    .build();
        }
    }
}
