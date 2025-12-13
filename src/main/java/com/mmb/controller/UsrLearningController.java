package com.mmb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmb.dto.QuizQuestionDto;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.service.FullLearningService;
import com.mmb.service.LearningService;
import com.mmb.service.MemberService;
import com.mmb.service.QuizQuestionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
public class UsrLearningController {

    private final LearningService learningService;
    private final FullLearningService fullLearningService;
    private final MemberService memberService;
    private final QuizQuestionService quizQuestionService;
    private final ObjectMapper objectMapper;

    @GetMapping("/wordbook")
    public String showWordbook(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        Member member = memberService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Member not found."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);
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
    public Map<String, Object> recordQuizResult(@RequestParam Long wordId,
                                                @RequestParam boolean correct,
                                                HttpSession session,
                                                Principal principal) {

        Map<String, Object> response = new HashMap<>();

        Long memberId = resolveMemberId(session, principal);
        if (memberId == null) {
            response.put("success", false);
            response.put("msg", "NOT_LOGGED_IN");
            return response;
        }

        try {
            fullLearningService.applyQuizResult(memberId, wordId, correct);
        } catch (Exception e) {
            response.put("success", false);
            response.put("msg", "SAVE_FAILED");
            return response;
        }

        long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(memberId);
        int todayTarget = fullLearningService.getDailyTarget(memberId);
        long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);

        response.put("success", true);
        response.put("quizSolvedCount", quizSolvedCount);
        response.put("quizRemainingCount", quizRemainingCount);

        return response;
    }

    private Long resolveMemberId(HttpSession session, Principal principal) {
        if (principal != null) {
            return memberService.findByUsername(principal.getName())
                    .map(member -> {
                        session.setAttribute("loginedMemberId", member.getId());
                        return member.getId();
                    })
                    .orElse(null);
        }

        Object attribute = session.getAttribute("loginedMemberId");
        if (attribute instanceof Long id) {
            return id;
        }

        return null;
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
