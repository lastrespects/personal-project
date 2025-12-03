package com.mmb.legacy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiWordItem {
    private String spelling; // 단어
    private String meaning;  // 의미
    private String example;  // 예문
    private String audioFile; // 오디오 경로
}