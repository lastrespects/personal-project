// src/main/java/com/mmb/api/TranslationClient.java
package com.mmb.api;

public interface TranslationClient {

    /**
     * 영어 문장을 한국어로 번역
     */
    String translateToKorean(String englishText);
}

