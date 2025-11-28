// WordDto.java
package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordDto {
    private String spelling;
    private String meaning;
    private String exampleSentence;
    private String audioPath;
}