package com.mmb.api;

public interface TranslationClient {

    /**
     * Translate an English sentence to Korean.
     */
    String translateToKorean(String englishText);
}