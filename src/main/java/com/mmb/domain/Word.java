package com.mmb.domain;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter @NoArgsConstructor
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spelling;       // apple
    private String meaning;        // 사과
    
    @Column(length = 500)
    private String exampleSentence; // "I ate an _____ for breakfast." (빈칸 문제용)
    
    private String audioPath;      // TTS 파일 경로

    public Word(String spelling, String meaning, String exampleSentence) {
        this.spelling = spelling;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
    }
}