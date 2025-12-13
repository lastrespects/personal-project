package com.mmb.controller;

import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.service.FullLearningService;
import com.mmb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/usr/study")
@RequiredArgsConstructor
public class UsrStudyController {

    private final MemberService memberService;
    private final FullLearningService fullLearningService;

    @GetMapping("/log")
    public String showStudyLog(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?msg=PleaseLogin";
        }

        Member member = memberService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<StudyRecord> records = fullLearningService.getRecentStudyRecords(member.getId(), 100);
        model.addAttribute("records", records);
        return "usr/study/log";
    }
}
