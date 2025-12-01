package com.mmb.service;

import com.google.gson.Gson;
import com.mmb.dto.StudyRecordDto;
import com.mmb.entity.Member;
import com.mmb.entity.StudyRecord;
import com.mmb.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final MemberService memberService;
    private final StudyRecordRepository studyRecordRepository;
    private final LikePointService likePointService;
    private final WordService wordService;
    private final Gson gson;

    @Transactional(readOnly = true)
    public List<StudyRecordDto> generateDailyQuizDto(int memberId) {

        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Invalid member ID");
        }

        int dailyTarget = member.getDailyTarget();

        List<StudyRecord> todayReviews =
                studyRecordRepository.findTodayReviews(memberId, LocalDate.now());

        int remaining = Math.max(0, dailyTarget - todayReviews.size());

        // TODO: remaining 만큼 새 단어 섞어서 퀴즈 만드는 로직은 나중에 구현

        List<StudyRecordDto> quiz = new ArrayList<>();
        for (StudyRecord sr : todayReviews) {
            StudyRecordDto dto = new StudyRecordDto();
            dto.setId(sr.getId());
            // WordDto 매핑은 나중에
            quiz.add(dto);
        }

        return quiz;
    }

    @Transactional
    public String toggleLikeStudyRecord(int memberId, int studyRecordId) {
        // TODO: 아직 구현X
        return "좋아요 토글 성공";
    }

    @Transactional
    public void markWrong(int studyRecordId) {
        // TODO: 아직 구현X
    }
}
