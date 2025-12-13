package com.mmb.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryResponse {

    private String word;
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Definition {
        private String definition;
        private String example;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Phonetic {
        private String text;
        private String audio;
    }
}
