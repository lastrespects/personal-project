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

    // 생성일
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    // FK: memberId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    // FK: wordId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordId", nullable = false)
    private Word word;

    @Column(nullable = false)
    @Builder.Default
    private Integer correctCount = 0; // 정답 횟수

    @Column(nullable = false)
    @Builder.Default
    private Integer incorrectCount = 0; // 오답 횟수

    @Column(nullable = false)
    @Builder.Default
    private Integer totalAttempts = 0; // 총 시도 횟수

    private LocalDateTime lastReviewDate; // 마지막 학습 일시

    // ====== 라이프사이클 콜백 ======

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.regDate = now;
        if (this.lastReviewDate == null) {
            this.lastReviewDate = now;
        }
        if (this.correctCount == null)
            this.correctCount = 0;
        if (this.incorrectCount == null)
            this.incorrectCount = 0;
        if (this.totalAttempts == null)
            this.totalAttempts = 0;
    }

    // ====== SRS용 편의 메서드 ======

    public void recordResult(boolean isCorrect) {
        if (totalAttempts == null)
            totalAttempts = 0;
        if (correctCount == null)
            correctCount = 0;
        if (incorrectCount == null)
            incorrectCount = 0;

        totalAttempts++;
        if (isCorrect) {
            correctCount++;
        } else {
            incorrectCount++;
        }
        lastReviewDate = LocalDateTime.now();
    }
}
