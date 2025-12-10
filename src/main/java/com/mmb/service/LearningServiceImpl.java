package com.mmb.service;

import com.mmb.dto.TodayWordDto;
import com.mmb.entity.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final FullLearningService fullLearningService;
    private final com.mmb.repository.MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TodayWordDto> prepareTodayWords(Long memberId) {
        com.mmb.entity.Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Delegate to FullLearningService to get the list of words for today (SRS +
        // New)
        List<Word> words = fullLearningService.buildTodayQuizWordsV2(memberId);

        return words.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TodayWordDto convertToDto(Word w) {
        return TodayWordDto.builder()
                .wordId(w.getId())
                .spelling(w.getSpelling())
                .meaning(w.getMeaning())
                .exampleSentence(w.getExampleSentence())
                .audioPath(w.getAudioPath())
                .review(true) // Assuming all returned are for review/study
                .build();
    }
}
