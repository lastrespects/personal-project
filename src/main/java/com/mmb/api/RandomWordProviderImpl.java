package com.mmb.api;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomWordProviderImpl implements RandomWordProvider {

    private static final List<String> BASIC_WORDS = List.of(
            "apple", "study", "language", "computer", "spring", "coffee", "music", "travel", "friend", "project",
            "water", "river", "mountain", "forest", "ocean", "cloud", "rain", "snow", "wind", "storm",
            "teacher", "student", "school", "library", "book", "pencil", "paper", "desk", "chair", "blackboard",
            "city", "village", "country", "garden", "park", "road", "bridge", "station", "airport", "harbor",
            "family", "mother", "father", "sister", "brother", "grandmother", "grandfather", "uncle", "aunt", "cousin",
            "dog", "cat", "bird", "fish", "horse", "cow", "sheep", "pig", "chicken", "tiger",
            "car", "bus", "train", "bicycle", "boat", "plane", "subway", "truck", "motorcycle", "taxi",
            "phone", "camera", "television", "radio", "computer", "laptop", "tablet", "keyboard", "mouse", "screen",
            "music", "movie", "drama", "concert", "festival", "game", "puzzle", "sport", "soccer", "basketball",
            "breakfast", "lunch", "dinner", "bread", "rice", "noodle", "soup", "salad", "fruit", "vegetable",
            "doctor", "nurse", "hospital", "medicine", "health", "exercise", "yoga", "run", "walk", "sleep",
            "happy", "sad", "angry", "excited", "nervous", "bored", "tired", "surprised", "calm", "proud",
            "plan", "goal", "dream", "idea", "solution", "problem", "question", "answer", "result", "progress",
            "culture", "history", "future", "present", "past", "nature", "science", "technology", "art", "design",
            "team", "leader", "member", "manager", "meeting", "project", "task", "report", "schedule", "deadline",
            "money", "bank", "market", "shop", "store", "company", "office", "factory", "service", "product",
            "internet", "website", "email", "message", "password", "account", "login", "logout", "profile", "setting",
            "korea", "seoul", "busan", "daegu", "incheon", "gwangju", "daejeon", "ulsan", "jeju", "asia",
            "english", "korean", "japanese", "chinese", "spanish", "french", "german", "russian", "italian", "arabic"
    );

    private final Random random = new Random();

    @Override
    public String getRandomEnglishWord() {
        // 나중에 실제 Random Word API로 교체 가능
        int idx = random.nextInt(BASIC_WORDS.size());
        return BASIC_WORDS.get(idx);
    }
}
