package com.mmb.api;

public interface DictionaryApiClient {

    DictionaryResponse lookup(String word);

    public record DictionaryWordInfo(
            String spelling,
            String definition,
            String example,
            String audioUrl
    ) {
        public String getSpelling() { return spelling; }
        public String getDefinition() { return definition; }
        public String getExample() { return example; }
        public String getAudioUrl() { return audioUrl; }
    }

    DictionaryWordInfo fetchWordInfo(String word);
}
