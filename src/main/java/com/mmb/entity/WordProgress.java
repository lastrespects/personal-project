package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "word_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_member_word", columnNames = {"memberId", "wordId"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ INT PK

    @Column(name = "memberId", nullable = false)
    private Integer memberId;

    @Column(name = "wordId", nullable = false)
    private Integer wordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", referencedColumnName = "id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordId", referencedColumnName = "id", insertable = false, updatable = false)
    private Word word;

    @Column(nullable = false)
    @Builder.Default
    private int wrongStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private int correctCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int wrongCount = 0;

    private LocalDate nextReviewDate;
    private LocalDate lastStudiedDate;
}
