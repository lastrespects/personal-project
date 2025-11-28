package com.mmb.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MyBatis Member 테이블과 연동하기 위해 ID만 저장
    @Column(name = "member_id", nullable = false)
    private int memberId; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    private int reviewStep;
    private int wrongCount;
    private LocalDate nextReviewDate;

    @Builder
    public StudyRecord(int memberId, Word word, int reviewStep, int wrongCount, LocalDate nextReviewDate) {
        this.memberId = memberId;
        this.word = word;
        this.reviewStep = reviewStep;
        this.wrongCount = wrongCount;
        this.nextReviewDate = nextReviewDate;
    }

    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }
}