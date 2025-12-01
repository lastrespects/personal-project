package com.mmb.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RandomWordClient {

    private final WebClient webClient;

    @Value("${randomword.api.base-url}")
    private String randomWordBaseUrl;

    public List<String> getRandomWords(int count) {
        try {
            List<String> words = webClient.get()
                    .uri(randomWordBaseUrl + "/api?words=" + count)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (words == null) {
                return Collections.emptyList();
            }
            return words;
        } catch (Exception e) {
            System.err.println("[RandomWordClient] 단어 가져오기 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
