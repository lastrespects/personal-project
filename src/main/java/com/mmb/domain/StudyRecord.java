package com.mmb.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class StudyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "word_id")
    private Word word;

    private int reviewStep; // 망각곡선 단계 (0~5)
    private LocalDate nextReviewDate; // 다음 복습일
    private int wrongCount; // 틀린 횟수

    public StudyRecord(Member member, Word word) {
        this.member = member;
        this.word = word;
        this.reviewStep = 0;
        this.wrongCount = 0;
        this.nextReviewDate = LocalDate.now(); // 당장 학습
    }
}