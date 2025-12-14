package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayWordDto {

    // 단어 ID (지금은 화면에서 안 쓰더라도 있어도 무방)
    private Integer wordId;

    // 영어 단어
    private String spelling;

    // 한글 뜻 (DB / 번역 / 사전 결과)
    private String meaning;

    // 예문 또는 "예문이 없습니다."
    private String exampleSentence;

    // 발음 파일 경로
    private String audioPath;

    // 복습 여부 (오늘 학습 리스트에서 항상 true로 세팅)
    private boolean review;
}
