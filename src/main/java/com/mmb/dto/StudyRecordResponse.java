package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class StudyRecordResponse {
    private LocalDateTime nextReviewAt;
    private int correctStreak;
    private int intervalDays;
    private double easeFactor;
}
