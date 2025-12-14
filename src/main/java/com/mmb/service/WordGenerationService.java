// src/main/java/com/mmb/service/learning/WordGenerationService.java
package com.mmb.service;

import java.util.List;

import com.mmb.entity.Member;
import com.mmb.entity.Word;

public interface WordGenerationService {

    List<Word> generateNewWordsForMember(Member member, int count, java.util.Set<Integer> excludeWordIds);
}
