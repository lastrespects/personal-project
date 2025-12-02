package com.mmb.api;

import org.springframework.stereotype.Component;

/**
 * DictionaryApiClient의 임시 구현체.
 * 지금은 서버를 띄우기 위한 "더미 버전"이고,
 * 실제 DictionaryAPI.dev 연동은 나중에 구현해도 된다.
 */
@Component
public class DictionaryApiClientImpl implements DictionaryApiClient {

    @Override
    public DictionaryResponse lookup(String word) {
        // TODO: 나중에 DictionaryAPI.dev 실제 호출로 교체
        // 지금은 null을 리턴해서, WordGenerationServiceImpl에서
        // "사전 응답 없음 → 단어 자체를 번역" 로직이 타도록 한다.
        return null;
    }

    @Override
    public DictionaryWordInfo fetchWordInfo(String word) {
        // TODO: 나중에 실제 사전 응답을 기반으로 채워줄 것
        // 일단은 컴파일/실행용 더미 데이터
        return new DictionaryWordInfo(
                word,         // spelling
                word,         // definition (임시로 단어 자체)
                null,         // example
                null          // audioUrl
        );
    }
}
