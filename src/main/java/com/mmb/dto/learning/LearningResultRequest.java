// src/main/java/com/mmb/dto/learning/LearningResultRequest.java
package com.mmb.dto.learning;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningResultRequest {

    private Long wordId;
    private boolean correct;
}
