package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_record")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime regDate;

    private Long memberId;
    private Long wordId;

    private Integer correctCount = 0;
    private Integer incorrectCount = 0;
    private Integer totalAttempts = 0;
    private LocalDateTime lastReviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordId", insertable = false, updatable = false)
    private Word word;

    @PrePersist
    protected void onCreate() {
        regDate = LocalDateTime.now();
        lastReviewDate = LocalDateTime.now();
    }
}
