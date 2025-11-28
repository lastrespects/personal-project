package com.mmb.external.dto;

import lombok.Data;
import java.util.List;

/**
 * dictionaryapi.dev 반환 구조를 단순 매핑
 * (외부 응답은 변동될 수 있으므로 별도 패키지에 둠)
 */
@Data
public class DictionaryApiResponse {
    private String word;
    private List<Meaning> meanings;

    @Data
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Data
    public static class Definition {
        private String definition;
        private String example;
    }
}
