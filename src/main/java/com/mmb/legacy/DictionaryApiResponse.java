package com.mmb.legacy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

// API 응답의 Root 구조를 담을 DTO (API 형태에 따라 변경될 수 있음)
@Getter
@Setter
@ToString
public class DictionaryApiResponse {
    // 공공 API 응답이 Map<String, Object>로 복잡하게 들어왔다면, 
    // 실제 단어 목록을 담는 Item List를 포함한 DTO를 정의해야 합니다.
    // 여기서는 가장 일반적인 형태로 가정합니다.
    private List<ApiWordItem> words;
    // ... 다른 메타데이터 필드 (예: totalCount)
}

