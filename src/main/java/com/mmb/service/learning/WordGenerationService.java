// src/main/java/com/mmb/service/learning/WordGenerationService.java
package com.mmb.service.learning;

import com.mmb.entity.Member;
import com.mmb.entity.Word;

import java.util.List;

public interface WordGenerationService {

    List<Word> generateNewWordsForMember(Member member, int count);
}
