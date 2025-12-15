package com.mmb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/learning") // ✅ 여기서 /learning 으로 통일
@RequiredArgsConstructor
@Slf4j
public class UsrLearningController {

    private final LearningService learningService;
    private final FullLearningService fullLearningService;
    private final MemberService memberService;
    private final QuizQuestionService quizQuestionService; // (지금은 안 쓰지만 의존성 유지)
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
    public String showQuiz(Model model) {
        Integer memberId = req.getLoginedMemberId();
        if (memberId == null) {
            return "redirect:/usr/member/login?error=1";
        }

        Member member = req.getLoginedMember();
        if (member == null) {
            member = memberService.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found."));
        }

        model.addAttribute("memberId", member.getId());

        long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(member.getId());
        int todayTarget = fullLearningService.getDailyTarget(member.getId());
        long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);
        long quizCorrectCount = fullLearningService.getTodayQuizCorrectCount(member.getId());

        model.addAttribute("quizSolvedCount", quizSolvedCount);
        model.addAttribute("quizRemainingCount", quizRemainingCount);
        model.addAttribute("quizCorrectCount", quizCorrectCount);
        model.addAttribute("dailyTarget", todayTarget);

        List<Word> todayWords = fullLearningService.ensureTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        try {
            String json = objectMapper.writeValueAsString(buildWordFallback(todayWords));

            // ✅ <script> 깨짐/보안 방지 (JSON을 JS로 직접 주입할 때 필수)
            json = json.replace("<", "\\u003c")
                       .replace(">", "\\u003e")
                       .replace("&", "\\u0026");

            model.addAttribute("wordsJson", json);
        } catch (JsonProcessingException e) {
            log.error("[QUIZ_JSON_FAIL] memberId={}", memberId, e);
            // 실패해도 화면은 뜨게
            model.addAttribute("wordsJson", "[]");
        }

        return "usr/learning/quiz";
    }

    @PostMapping("/quiz/result") // ✅ 저장도 /learning/quiz/result 로 통일
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
            log.error("[QUIZ_SAVE_FAIL] endpoint=/learning/quiz/result memberId={}, wordId={}", memberId, wordId, e);
            String message = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage()
                    : "퀴즈 저장에 실패했습니다.";
            return ResultData.from("F-500", message);
        }

        long quizSolvedCount = fullLearningService.getTodayQuizSolvedCount(memberId);
        int todayTarget = fullLearningService.getDailyTarget(memberId);
        long quizRemainingCount = Math.max(todayTarget - quizSolvedCount, 0);
        long quizCorrectCount = fullLearningService.getTodayQuizCorrectCount(memberId);

        Map<String, Object> data = new HashMap<>();
        data.put("quizSolvedCount", quizSolvedCount);
        data.put("quizRemainingCount", quizRemainingCount);
        data.put("quizCorrectCount", quizCorrectCount);

        return ResultData.from("S-1", "QUIZ_RESULT_SAVED", data);
    }

    private List<Map<String, Object>> buildWordFallback(List<Word> todayWords) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (todayWords == null) return list;

        for (Word w : todayWords) {
            if (w == null) continue;

            String spelling = normalize(w.getSpelling());
            String meaning = normalize(w.getMeaning());
            String example = normalize(w.getExampleSentence());
            String audioPath = normalize(w.getAudioPath());

            if (spelling.isBlank()) continue;
            if (meaning.isBlank()) meaning = "의미 미확인";

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
