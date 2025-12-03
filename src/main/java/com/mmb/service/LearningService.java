// src/main/java/com/mmb/service/learning/LearningService.java
package com.mmb.service;

import java.util.List;

import com.mmb.dto.TodayWordDto;

public interface LearningService {

    List<TodayWordDto> prepareTodayWords(Long memberId);

    void recordResult(Long memberId, Long wordId, boolean correct);
}
