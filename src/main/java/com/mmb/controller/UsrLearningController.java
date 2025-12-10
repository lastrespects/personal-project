package com.mmb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.entity.Word;
import com.mmb.service.FullLearningService;
import com.mmb.service.LearningService;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
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

        // ✅ Use FullLearningService V2 for SRS-based quiz generation
        List<Word> todayWords = fullLearningService.buildTodayQuizWordsV2(member.getId());
        model.addAttribute("todayWords", todayWords);

        List<Map<String, Object>> quizWords = new ArrayList<>();
        for (Word w : todayWords) {
            String spelling = normalize(w.getSpelling());
            String meaning = normalize(w.getMeaning());
            // Skip words that lack either spelling (en) or meaning (ko)
            if (spelling.isBlank() || meaning.isBlank()) {
                continue;
            }

            String example = normalize(w.getExampleSentence());
            String audioPath = normalize(w.getAudioPath());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", w.getId() == null ? -1 : w.getId());
            item.put("spelling", spelling);
            item.put("meaning", meaning);
            item.put("example", example);
            item.put("audioPath", audioPath);
            quizWords.add(item);
        }

        try {
            model.addAttribute("wordsJson", objectMapper.writeValueAsString(quizWords));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize quiz data.", e);
        }

        return "usr/learning/quiz";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("\r", " ").replace("\n", " ").trim();
        if ("null".equalsIgnoreCase(cleaned)) {
            return "";
        }
        return cleaned;
    }
}
