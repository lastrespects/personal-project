// src/main/java/com/mmb/dto/learning/TodayWordDto.java
package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayWordDto {

    private Long wordId;
    private String spelling;
    private String meaning;
    private String exampleSentence;
    private String audioPath;

    private boolean review; // true = 복습 단어, false = 새 단어
}
