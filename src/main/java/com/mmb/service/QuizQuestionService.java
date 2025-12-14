package com.mmb.service;

import com.mmb.api.TranslationClient;
import com.mmb.dto.QuizQuestionDto;
import com.mmb.entity.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizQuestionService {

    private static final String TYPE_MCQ_MEANING = "MCQ_MEANING";
    private static final String TYPE_MCQ_WORD = "MCQ_WORD";
    private static final String TYPE_CLOZE = "CLOZE";
    private static final String TYPE_LISTEN = "LISTEN_MCQ";
    private static final String TYPE_SPELLING = "SPELLING_INPUT";
    private static final String TYPE_SCRAMBLE = "SCRAMBLE";

    private static final String DIR_EN_TO_KO = "EN_TO_KO";
    private static final String DIR_KO_TO_EN = "KO_TO_EN";
    private static final String FALLBACK_MEANING = "뜻 준비중";
    private static final String LISTEN_PROMPT = "발음을 듣고 뜻을 고르세요.";

    private final TranslationClient translationClient;
    private final SecureRandom random = new SecureRandom();

    /**
     * 오늘의 Word 리스트로부터 서버에서 바로 렌더 가능한 QuizQuestionDto 리스트 생성
     */
    public List<QuizQuestionDto> buildQuestions(List<Word> words) {
        if (words == null || words.isEmpty()) {
            return List.of();
        }

        List<WordContext> contexts = words.stream()
                .map(this::toContext)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (contexts.isEmpty()) {
            return List.of();
        }

        List<QuizQuestionDto> questions = new ArrayList<>();

        List<String> meaningPool = contexts.stream()
                .map(WordContext::meaning)
                .collect(Collectors.toList());

        List<String> spellingPool = contexts.stream()
                .map(WordContext::spelling)
                .collect(Collectors.toList());

        boolean startWithEnToKo = random.nextBoolean();

        // 기본 EN↔KO MCQ를 번갈아가며 생성
        for (int i = 0; i < contexts.size(); i++) {
            WordContext ctx = contexts.get(i);
            boolean useEnToKo = (i % 2 == 0) == startWithEnToKo;

            QuizQuestionDto question = useEnToKo
                    ? buildEnToKo(ctx, meaningPool)
                    : buildKoToEn(ctx, spellingPool);

            questions.add(question);
        }

        // 일부 문제를 CLOZE/LISTEN/SPELLING/SCRAMBLE 로 변형
        applyOptionalVariants(contexts, questions, meaningPool, spellingPool);

        logDirectionStats(questions);
        return questions;
    }

    // ---------------- core builders ----------------

    private WordContext toContext(Word word) {
        String spelling = normalize(word.getSpelling());
        if (spelling.isBlank()) {
            log.warn("[QUIZ WORD SKIP] Missing spelling for wordId={}", word.getId());
            return null;
        }

        String meaning = resolveMeaning(word, spelling);
        String example = normalize(word.getExampleSentence());
        String audio = normalize(word.getAudioPath());

        return new WordContext(word.getId(), spelling, meaning, example, audio);
    }

    private QuizQuestionDto buildEnToKo(WordContext ctx, List<String> meaningPool) {
        List<String> options = buildOptions(ctx.meaning, meaningPool);
        return baseBuilder(ctx)
                .type(TYPE_MCQ_MEANING)
                .direction(DIR_EN_TO_KO)
                .prompt(ctx.spelling)
                .answer(ctx.meaning)
                .correct(ctx.meaning)
                .options(options.toArray(new String[0]))
                .build();
    }

    private QuizQuestionDto buildKoToEn(WordContext ctx, List<String> spellingPool) {
        List<String> options = buildOptions(ctx.spelling, spellingPool);
        return baseBuilder(ctx)
                .type(TYPE_MCQ_WORD)
                .direction(DIR_KO_TO_EN)
                .prompt(ctx.meaning)
                .answer(ctx.spelling)
                .correct(ctx.spelling)
                .options(options.toArray(new String[0]))
                .build();
    }

    private void applyOptionalVariants(List<WordContext> contexts,
                                       List<QuizQuestionDto> questions,
                                       List<String> meaningPool,
                                       List<String> spellingPool) {

        for (int i = 0; i < contexts.size(); i++) {
            WordContext ctx = contexts.get(i);
            QuizQuestionDto original = questions.get(i);
            double roll = random.nextDouble();
            String direction = original.getDirection();

            // 1) CLOZE: KO→EN 문제이면서 예문에 영어가 실제로 들어 있는 경우
            if (roll < 0.10
                    && DIR_KO_TO_EN.equals(direction)
                    && isNotBlank(ctx.example)
                    && ctx.example.toLowerCase(Locale.ROOT).contains(ctx.spelling.toLowerCase(Locale.ROOT))) {

                QuizQuestionDto cloze = buildClozeQuestion(ctx, direction, spellingPool);
                if (cloze != null) {
                    questions.set(i, cloze);
                    continue;
                }
            }

            // 2) LISTEN: EN→KO 문제이면서 오디오가 있는 경우
            if (roll >= 0.10 && roll < 0.20
                    && DIR_EN_TO_KO.equals(direction)
                    && isNotBlank(ctx.audioPath)) {

                QuizQuestionDto listen = buildListenQuestion(ctx, meaningPool);
                if (listen != null) {
                    questions.set(i, listen);
                    continue;
                }
            }

            // 3) SPELLING_INPUT: KO→EN 문제 + 한글 의미가 있을 때
            if (roll >= 0.20 && roll < 0.28
                    && DIR_KO_TO_EN.equals(direction)
                    && containsHangul(ctx.meaning)) {

                QuizQuestionDto spellingInput = buildSpellingInput(ctx);
                if (spellingInput != null) {
                    questions.set(i, spellingInput);
                    continue;
                }
            }

            // 4) SCRAMBLE: KO→EN 문제 + 철자가 4글자 이상일 때
            if (roll >= 0.28 && roll < 0.35
                    && DIR_KO_TO_EN.equals(direction)
                    && ctx.spelling.length() >= 4) {

                QuizQuestionDto scramble = buildScramble(ctx);
                if (scramble != null) {
                    questions.set(i, scramble);
                } else {
                    questions.set(i, original);
                }
            }
        }
    }

    private QuizQuestionDto buildClozeQuestion(WordContext ctx,
                                               String direction,
                                               List<String> spellingPool) {

        String placeholder = "____";
        String regex = "(?i)" + java.util.regex.Pattern.quote(ctx.spelling);
        String clozed = ctx.example.replaceAll(regex, placeholder);
        if (clozed.equals(ctx.example)) {
            return null;
        }

        List<String> options = buildOptions(ctx.spelling, spellingPool);
        return baseBuilder(ctx)
                .type(TYPE_CLOZE)
                .direction(direction)
                .prompt(clozed)
                .answer(ctx.spelling)
                .correct(ctx.spelling)
                .options(options.toArray(new String[0]))
                .example(ctx.example)
                .build();
    }

    private QuizQuestionDto buildListenQuestion(WordContext ctx, List<String> meaningPool) {
        List<String> options = buildOptions(ctx.meaning, meaningPool);
        return baseBuilder(ctx)
                .type(TYPE_LISTEN)
                .direction(DIR_EN_TO_KO)
                .prompt(LISTEN_PROMPT)
                .answer(ctx.meaning)
                .correct(ctx.meaning)
                .options(options.toArray(new String[0]))
                .audioPath(ctx.audioPath)
                .build();
    }

    private QuizQuestionDto buildSpellingInput(WordContext ctx) {
        return baseBuilder(ctx)
                .type(TYPE_SPELLING)
                .direction(DIR_KO_TO_EN)
                .prompt(ctx.meaning)
                .answer(ctx.spelling)
                .correct(ctx.spelling)
                .options(new String[0]) // 주관식
                .build();
    }

    private QuizQuestionDto buildScramble(WordContext ctx) {
        List<Character> chars = ctx.spelling.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        StringBuilder scrambled = new StringBuilder();
        chars.forEach(scrambled::append);

        if (scrambled.toString().equalsIgnoreCase(ctx.spelling)) {
            return null;
        }

        return baseBuilder(ctx)
                .type(TYPE_SCRAMBLE)
                .direction(DIR_KO_TO_EN)
                .prompt(scrambled.toString())
                .answer(ctx.spelling)
                .correct(ctx.spelling)
                .options(new String[0]) // 주관식
                .build();
    }

    private QuizQuestionDto.QuizQuestionDtoBuilder baseBuilder(WordContext ctx) {
        return QuizQuestionDto.builder()
                .wordId(ctx.wordId)
                .example(ctx.example)
                .audioPath(ctx.audioPath)
                .spelling(ctx.spelling)
                .meaning(ctx.meaning);
    }

    // ---------------- helpers ----------------

    private List<String> buildOptions(String correct, List<String> pool) {
        List<String> candidates = new ArrayList<>(pool);
        candidates.removeIf(v -> v == null || v.isBlank());
        Collections.shuffle(candidates, random);

        List<String> options = new ArrayList<>();
        options.add(correct);

        for (String candidate : candidates) {
            if (options.size() >= 4) break;
            if (candidate.equalsIgnoreCase(correct)) continue;
            if (options.stream().anyMatch(opt -> opt.equalsIgnoreCase(candidate))) continue;
            options.add(candidate);
        }

        while (options.size() < 4) {
            options.add(correct);
        }

        Collections.shuffle(options, random);
        return options;
    }

    private void logDirectionStats(List<QuizQuestionDto> questions) {
        Map<String, Long> stats = new HashMap<>();
        for (QuizQuestionDto dto : questions) {
            if (dto.getDirection() == null) continue;
            stats.merge(dto.getDirection(), 1L, Long::sum);
        }
        log.info("[QUIZ QUESTION DEBUG] total={}, stats={}", questions.size(), stats);
    }

    private boolean containsHangul(String value) {
        return value != null && value.matches(".*[\\u3131-\\u318E\\uAC00-\\uD7A3].*");
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        if (value == null) return "";
        String cleaned = value.replace("\r", " ")
                              .replace("\n", " ")
                              .trim();
        if ("null".equalsIgnoreCase(cleaned)) return "";
        return cleaned;
    }

    /**
     * 의미 결정: DB → 번역 → 로컬 사전 → fallback
     */
    private String resolveMeaning(Word word, String spelling) {
        String meaning = normalize(word.getMeaning());
        if (containsHangul(meaning)) {
            return meaning;
        }

        String translated = safeTranslate(spelling);
        if (containsHangul(translated)) {
            return translated;
        }

        String dictionaryMeaning = lookupDictionary(spelling);
        if (containsHangul(dictionaryMeaning)) {
            return dictionaryMeaning;
        }

        log.warn("[QUIZ WORD FALLBACK] spelling={}, rawMeaning={}, translated={}",
                spelling, word.getMeaning(), translated);
        return FALLBACK_MEANING + " (" + spelling + ")";
    }

    private String safeTranslate(String spelling) {
        try {
            return normalize(translationClient.translateToKorean(spelling));
        } catch (Exception e) {
            log.warn("[QUIZ TRANSLATE ERROR] word={}, msg={}", spelling, e.getMessage());
            return "";
        }
    }

    private String lookupDictionary(String spelling) {
        if (spelling == null) return "";
        String value = LocalMeaningDictionary.MEANINGS
                .getOrDefault(spelling.toLowerCase(Locale.ROOT), "");
        return normalize(value);
    }

    private record WordContext(
            Integer wordId,
            String spelling,
            String meaning,
            String example,
            String audioPath
    ) {}
}
