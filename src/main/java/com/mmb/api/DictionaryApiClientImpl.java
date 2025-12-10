package com.mmb.api;

import org.springframework.stereotype.Component;

@Component
public class DictionaryApiClientImpl implements DictionaryApiClient {

    @Override
    public DictionaryResponse lookup(String word) {
        return null;
    }

    @Override
    public DictionaryWordInfo fetchWordInfo(String word) {
        String lower = word.toLowerCase();
        switch (lower) {
            case "apple":
                return new DictionaryWordInfo(word, "사과", "I ate a fresh apple.", googleTts(word));
            case "study":
                return new DictionaryWordInfo(word, "공부하다", "She will study after dinner.", googleTts(word));
            case "language":
                return new DictionaryWordInfo(word, "언어", "Language connects people.", googleTts(word));
            case "computer":
                return new DictionaryWordInfo(word, "컴퓨터", "The computer is on the desk.", googleTts(word));
            case "spring":
                return new DictionaryWordInfo(word, "봄", "Spring is my favorite season.", googleTts(word));
            case "coffee":
                return new DictionaryWordInfo(word, "커피", "He drinks coffee every morning.", googleTts(word));
            case "music":
                return new DictionaryWordInfo(word, "음악", "Music makes me happy.", googleTts(word));
            case "travel":
                return new DictionaryWordInfo(word, "여행하다", "They plan to travel abroad.", googleTts(word));
            case "friend":
                return new DictionaryWordInfo(word, "친구", "My friend is kind.", googleTts(word));
            case "project":
                return new DictionaryWordInfo(word, "프로젝트", "The project ends tomorrow.", googleTts(word));
            case "planet":
                return new DictionaryWordInfo(word, "행성", "Earth is a beautiful planet.", googleTts(word));
            case "library":
                return new DictionaryWordInfo(word, "도서관", "I study at the library.", googleTts(word));
            case "notebook":
                return new DictionaryWordInfo(word, "공책", "Write it in your notebook.", googleTts(word));
            case "pencil":
                return new DictionaryWordInfo(word, "연필", "Sharpen your pencil.", googleTts(word));
            case "memory":
                return new DictionaryWordInfo(word, "기억", "This song brings back a memory.", googleTts(word));
            case "ocean":
                return new DictionaryWordInfo(word, "대양", "The ocean is vast.", googleTts(word));
            case "mountain":
                return new DictionaryWordInfo(word, "산", "We climbed a high mountain.", googleTts(word));
            case "river":
                return new DictionaryWordInfo(word, "강", "The river flows fast.", googleTts(word));
            case "forest":
                return new DictionaryWordInfo(word, "숲", "The forest is quiet.", googleTts(word));
            case "future":
                return new DictionaryWordInfo(word, "미래", "Think about the future.", googleTts(word));
            default:
                return new DictionaryWordInfo(
                        word,
                        "뜻 준비중",
                        null,
                        googleTts(word)
                );
        }
    }

    private String googleTts(String word) {
        return "https://translate.google.com/translate_tts?ie=UTF-8&client=gtx&tl=en&q=" + word;
    }
}
