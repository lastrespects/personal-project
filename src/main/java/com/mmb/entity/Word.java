package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "word")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime regDate;
    private LocalDateTime updateDate;

    @Column(length = 100, nullable = false)
    private String spelling;

    @Column(length = 255, nullable = false)
    private String meaning;  // 한국어 뜻 (DeepL 번역 결과)

    @Lob
    private String exampleSentence; // 예문 (영어 or 번역)

    @Column(length = 255)
    private String audioPath; // 발음 mp3 URL

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (regDate == null) {
            regDate = LocalDateTime.now();
        }
        updateDate = LocalDateTime.now();
    }
}
