package com.mmb.external.client;

import com.mmb.external.dto.DictionaryApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * dictionaryapi.dev 호출기
 * - 실패 시 null 반환 (서비스 레벨에서 대체 전략 적용)
 */
@Component
public class DictionaryApiClient {
    private final RestTemplate rest = new RestTemplate();

    public DictionaryApiResponse fetch(String spelling) {
        try {
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + spelling;
            DictionaryApiResponse[] resp = rest.getForObject(url, DictionaryApiResponse[].class);
            if (resp != null && resp.length > 0) return resp[0];
        } catch (Exception e) {
            System.err.println("dictionary fetch failed for " + spelling + " : " + e.getMessage());
        }
        return null;
    }
}
