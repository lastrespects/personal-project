// src/main/java/com/mmb/service/learning/LearningService.java
package com.mmb.service;

import java.util.List;
import java.util.Map;

import com.mmb.dto.TodayWordDto;

public interface LearningService {

    List<TodayWordDto> prepareTodayWords(Integer memberId);

    void recordResult(Integer memberId, Integer wordId, boolean correct);

}
