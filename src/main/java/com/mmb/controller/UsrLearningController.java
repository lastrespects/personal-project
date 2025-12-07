package com.mmb.controller;

import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Member;
import com.mmb.service.LearningService;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/learning")
@RequiredArgsConstructor
public class UsrLearningController {

    private final LearningService learningService;
    private final MemberService memberService;

    @GetMapping("/wordbook")
    public String showWordbook(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        String username = principal.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        return "usr/learning/wordbook";
    }

    @GetMapping({ "/quiz", "/today" })
    public String showQuiz(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        String username = principal.getName();
        Member member = memberService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<TodayWordDto> todayWords = learningService.prepareTodayWords(member.getId());
        model.addAttribute("todayWords", todayWords);

        // JSON Serialization Logic moved here
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < todayWords.size(); i++) {
            TodayWordDto w = todayWords.get(i);
            String spelling = escapeJson(w.getSpelling());
            String meaning = escapeJson(w.getMeaning());
            String example = escapeJson(w.getExampleSentence());

            // Meaning fallback logic
            if (meaning.isBlank() || "null".equalsIgnoreCase(meaning)) {
                meaning = "(뜻 없음)";
            }

            jsonBuilder.append("{");
            jsonBuilder.append("\"id\":").append(w.getWordId()).append(",");
            jsonBuilder.append("\"spelling\":\"").append(spelling).append("\",");
            jsonBuilder.append("\"meaning\":\"").append(meaning).append("\",");
            jsonBuilder.append("\"example\":\"").append(example).append("\"");
            jsonBuilder.append("}");
            if (i < todayWords.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        model.addAttribute("wordsJson", jsonBuilder.toString());

        return "usr/learning/today";
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
