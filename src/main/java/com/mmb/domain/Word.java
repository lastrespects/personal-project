package com.mmb.domain;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 (JPA 필수)
public class Word {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spelling;       // 단어 (예: apple)
    
    @Column(length = 1000)         // 뜻이 길 수도 있어서 길이 늘림
    private String meaning;        // 뜻 (예: 사과)
    
    @Column(length = 1000)         // 예문도 길 수 있음
    private String exampleSentence; // 예문 (예: I ate an apple.)
    
    private String audioPath;      // ★ 추가됨: 음성 파일 URL

    // ★ 서비스(FullLearningService)에서 사용할 생성자
    public Word(String spelling, String meaning, String exampleSentence, String audioPath) {
        this.spelling = spelling;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.audioPath = audioPath;
    }
}