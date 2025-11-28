package com.mmb.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * StudyRecord를 클라이언트에 전달하기 위한 DTO
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StudyRecordDto {
    private Long id;
    private WordDto word;
    private int reviewStep;
    private int wrongCount;
    private LocalDate nextReviewDate;
}
