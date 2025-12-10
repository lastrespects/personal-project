package com.mmb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyRecordRequest {
    private Long wordId;
    private String questionType;
    private boolean isCorrect;
    private String userAnswer;
    private Long responseMs;
}
