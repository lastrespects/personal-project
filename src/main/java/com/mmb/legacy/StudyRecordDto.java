// StudyRecordDto.java
package com.mmb.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.mmb.dto.WordDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyRecordDto {
    private long id;
    private WordDto word;
    private int reviewStep;
    private int wrongCount;
    private LocalDate nextReviewDate;
}