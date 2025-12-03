// src/main/java/com/mmb/entity/Word.java
package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "word")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime regDate;
    private LocalDateTime updateDate;

    @Column(length = 100, nullable = false)
    private String spelling; // 단어 철자

    @Column(length = 255, nullable = false)
    private String meaning; // 한국어 뜻 (DeepL 번역 결과)

    @Lob
    private String exampleSentence; // 예문

    @Column(length = 255)
    private String audioPath; // TTS 오디오 or 사전 음성 URL

    @PrePersist
    public void onCreate() {
        this.regDate = LocalDateTime.now();
        this.updateDate = this.regDate;
    }

    @PreUpdate
    public void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
