package com.mmb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmb.dto.QuizQuestionDto;
import com.mmb.dto.Req;
import com.mmb.dto.ResultData;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.service.FullLearningService;
import com.mmb.service.LearningService;
import com.mmb.service.MemberService;
import com.mmb.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/learning")
@RequiredArgsConstructor
@Slf4j
public class UsrLearningController {

    private final LearningService learningService;
    private final FullLearningService fullLearningService;
    private final MemberService memberService;
    private final QuizQuestionService quizQuestionService;
    private final ObjectMapper objectMapper;
    private final Req req;

    @GetMapping("/wordbook")
    public String showWordbook(Model model) {
        Integer memberId = req.getLoginedMemberId();
        if (memberId == null) {
            return "redirect:/usr/member/login?error=1";
        }

        Member member = req.getLoginedMember();
        if (member == null) {
            member = memberService.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        }

        int dailyTarget = member.getDailyTarget() != null ? member.getDailyTarget()
                : fullLearningService.getDailyTarget(memberId);

        try {
            List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
            log.info("[WORDBOOK] memberId={} dailyTarget={} words={}", memberId, dailyTarget, todayWords.size());
            model.addAttribute("todayWords", todayWords);
            model.addAttribute("wordbookError", null);
        } catch (Exception e) {
            log.error("[WORDBOOK_FAIL] memberId={} dailyTarget={}", memberId, dailyTarget, e);
            model.addAttribute("todayWords", List.of());
            model.addAttribute("wordbookError", "단어장을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
        model.addAttribute("dailyTarget", dailyTarget);
        return "usr/learning/wordbook";
    }

    @GetMapping("/quiz")
    public String showQuiz(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        Member member = memberService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        // ✅ 메인/학습로그와 동일한 기준으로 오늘 푼 문제/남은 문제 계산
        long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(member.getId());
        int todayTarget = fullLearningService.getDailyTarget(member.getId());
        long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);

        model.addAttribute("quizSolvedCount", quizSolvedCount);
        model.addAttribute("quizRemainingCount", quizRemainingCount);

        // 오늘의 퀴즈 단어 & 서버에서 만든 질문 목록
        List<Word> todayWords = fullLearningService.buildTodayQuizWordsV2(member.getId());
        model.addAttribute("todayWords", todayWords);

        List<QuizQuestionDto> questions = quizQuestionService.buildQuestions(todayWords);

        try {
            model.addAttribute("questionsJson", objectMapper.writeValueAsString(questions));
            model.addAttribute("wordsJson", objectMapper.writeValueAsString(buildWordFallback(todayWords)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize quiz data.", e);
        }

        return "usr/learning/quiz";
    }

    @PostMapping("/quiz/result")
    @ResponseBody
    public ResultData<Map<String, Object>> recordQuizResult(@RequestParam Integer wordId,
                                                            @RequestParam boolean correct) {

        Integer memberId = req.getLoginedMemberId();
        if (memberId == null) {
            return ResultData.from("F-401", "로그인이 필요합니다.");
        }
        if (wordId == null || wordId <= 0) {
            return ResultData.from("F-400", "wordId가 없습니다.");
        }

        log.info("[QUIZ_SAVE] memberId={}, wordId={}, correct={}", memberId, wordId, correct);

        try {
            fullLearningService.applyQuizResult(memberId, wordId, correct);
        } catch (Exception e) {
            log.error("[WRITE_FAIL] endpoint=/learning/quiz/result memberId={}, wordId={}", memberId, wordId, e);
            String message = e.getMessage() != null ? e.getMessage() : "퀴즈 저장에 실패했습니다.";
            return ResultData.from("F-500", message);
        }

        long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(memberId);
        int todayTarget = fullLearningService.getDailyTarget(memberId);
        long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);

        Map<String, Object> data = new HashMap<>();
        data.put("quizSolvedCount", quizSolvedCount);
        data.put("quizRemainingCount", quizRemainingCount);

        return ResultData.from("S-1", "QUIZ_RESULT_SAVED", data);
    }

    // ✅ quiz.jsp에서 fallback wordsJson으로 쓰는 데이터
    private List<Map<String, Object>> buildWordFallback(List<Word> todayWords) {
        List<Map<String, Object>> list = new ArrayList<>();

        if (todayWords == null) {
            return list;
        }

        for (Word w : todayWords) {
            if (w == null) {
                continue;
            }

            String spelling = normalize(w.getSpelling());
            String meaning = normalize(w.getMeaning());
            String example = normalize(w.getExampleSentence());
            String audioPath = normalize(w.getAudioPath());

            if (spelling.isBlank()) {
                continue;
            }

            if (meaning.isBlank()) {
                meaning = "의미 미확인";
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", w.getId() == null ? -1 : w.getId());
            item.put("spelling", spelling);
            item.put("meaning", meaning);
            item.put("exampleSentence", example);
            item.put("audioPath", audioPath);

            list.add(item);
        }

        return list;
    }

    private String normalize(String value) {
        if (value == null) return "";
        String cleaned = value.replace("\r", " ").replace("\n", " ").trim();
        if ("null".equalsIgnoreCase(cleaned)) return "";
        return cleaned;
    }
}
