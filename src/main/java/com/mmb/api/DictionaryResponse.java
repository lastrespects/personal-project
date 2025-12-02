// src/main/java/com/mmb/api/DictionaryResponse.java
package com.mmb.api;

import lombok.*;

import java.util.List;

/**
 * DictionaryAPI.dev 응답을 단순화한 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryResponse {

    private List<Meaning> meanings;
    private List<Phonetic> phonetics;

    public String getFirstDefinitionOrNull() {
        if (meanings == null) return null;
        for (Meaning meaning : meanings) {
            if (meaning.getDefinitions() == null) continue;
            for (Definition def : meaning.getDefinitions()) {
                if (def.getDefinition() != null && !def.getDefinition().isBlank()) {
                    return def.getDefinition();
                }
            }
        }
        return null;
    }

    public String getFirstExampleOrNull() {
        if (meanings == null) return null;
        for (Meaning meaning : meanings) {
            if (meaning.getDefinitions() == null) continue;
            for (Definition def : meaning.getDefinitions()) {
                if (def.getExample() != null && !def.getExample().isBlank()) {
                    return def.getExample();
                }
            }
        }
        return null;
    }

    public String getFirstAudioUrlOrNull() {
        if (phonetics == null) return null;
        for (Phonetic p : phonetics) {
            if (p.getAudio() != null && !p.getAudio().isBlank()) {
                return p.getAudio();
            }
        }
        return null;
    }

    // 내부 DTO들
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Definition {
        private String definition;
        private String example;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Phonetic {
        private String text;
        private String audio;
    }
}
