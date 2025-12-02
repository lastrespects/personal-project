package com.mmb.api;

public interface RandomWordProvider {

    /**
     * 랜덤 영어 단어 하나를 반환.
     * 나중에 외부 API(Random Word API) 붙여도 되고,
     * 지금은 그냥 하드코딩 리스트에서 뽑아도 된다.
     */
    String getRandomEnglishWord();
}
