package com.mmb.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DictionaryApiClient {

    private final WebClient webClient;

    @Value("${dictionary.api.base-url}")
    private String baseUrl;

    public DictionaryWordInfo fetchWordInfo(String word) {
        try {
            String url = baseUrl + "/entries/en/" + word;

            DictionaryEntry[] entries = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(DictionaryEntry[].class)
                    .block();

            if (entries == null || entries.length == 0) return null;

            DictionaryEntry entry = entries[0];

            String spelling = entry.getWord();
            String audio = null;
            if (entry.getPhonetics() != null) {
                audio = entry.getPhonetics().stream()
                        .map(Phonetic::getAudio)
                        .filter(a -> a != null && !a.isBlank())
                        .findFirst()
                        .orElse(null);
            }

            String definition = null;
            String example = null;
            if (entry.getMeanings() != null && !entry.getMeanings().isEmpty()) {
                Meaning meaning = entry.getMeanings().get(0);
                if (meaning.getDefinitions() != null && !meaning.getDefinitions().isEmpty()) {
                    Definition def = meaning.getDefinitions().get(0);
                    definition = def.getDefinition();
                    example = def.getExample();
                }
            }

            DictionaryWordInfo info = new DictionaryWordInfo();
            info.setSpelling(spelling != null ? spelling : word);
            info.setDefinition(definition != null ? definition : "No definition.");
            info.setExample(example);
            info.setAudioUrl(audio);

            return info;

        } catch (Exception e) {
            System.err.println("[DictionaryApiClient] 호출 실패 (" + word + "): " + e.getMessage());
            return null;
        }
    }

    // ---------- 내부 DTO들 ----------

    @Data
    public static class DictionaryWordInfo {
        private String spelling;
        private String definition;
        private String example;
        private String audioUrl;
    }

    @Data
    public static class DictionaryEntry {
        private String word;
        private List<Phonetic> phonetics;
        private List<Meaning> meanings;
    }

    @Data
    public static class Phonetic {
        private String text;
        private String audio;
    }

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
