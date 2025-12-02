package com.mmb.api;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomWordProviderImpl implements RandomWordProvider {

    private static final List<String> BASIC_WORDS = List.of(
            "apple", "study", "language", "computer", "spring",
            "coffee", "music", "travel", "friend", "project"
    );

    private final Random random = new Random();

    @Override
    public String getRandomEnglishWord() {
        // 나중에 실제 Random Word API로 교체 가능
        int idx = random.nextInt(BASIC_WORDS.size());
        return BASIC_WORDS.get(idx);
    }
}
