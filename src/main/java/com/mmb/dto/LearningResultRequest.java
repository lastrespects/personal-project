// src/main/java/com/mmb/dto/learning/LearningResultRequest.java
package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningResultRequest {

    private Integer wordId;
    private boolean correct;
}
