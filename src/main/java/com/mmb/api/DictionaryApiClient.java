// src/main/java/com/mmb/api/DictionaryApiClient.java
package com.mmb.api;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface DictionaryApiClient {

    /**
     * DictionaryAPI.dev 전체 응답을 그대로 담은 DTO
     * (이미 프로젝트에 DictionaryResponse가 정의돼 있다고 가정)
     */
    DictionaryResponse lookup(String word);

    /**
     * WordService 등에서 쓰는 "간단 요약용" 정보 DTO
     */
    @Data
    @AllArgsConstructor
    class DictionaryWordInfo {
        private String spelling;    // ← getSpelling() 에러 해결용
        private String definition;
        private String example;
        private String audioUrl;
    }

    /**
     * 주어진 단어에 대한 간단 정보를 가져옴
     */
    DictionaryWordInfo fetchWordInfo(String word);
}
