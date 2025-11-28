package com.mmb.dto;

import lombok.*;

/**
 * 프론트/컨트롤러로 노출할 단어 DTO
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WordDto {
    private String spelling;
    private String meaning;
    private String exampleSentence;
    private String audioPath;
}
