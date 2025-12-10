package com.mmb.api;

import java.util.Optional;

public interface ExampleClient {
    /**
     * 영어 단어에 대한 예문을 하나 반환합니다.
     * 네트워크 오류나 결과 없음 시 Optional.empty() 반환.
     */
    Optional<String> fetchExample(String englishWord);
}
