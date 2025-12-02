// src/main/java/com/mmb/service/learning/LearningService.java
package com.mmb.service.learning;

import com.mmb.dto.learning.TodayWordDto;

import java.util.List;

public interface LearningService {

    List<TodayWordDto> prepareTodayWords(Long memberId);

    void recordResult(Long memberId, Long wordId, boolean correct);
}
