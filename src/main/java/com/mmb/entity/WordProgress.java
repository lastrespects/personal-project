package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "word_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "member_id", "word_id" }))
public class WordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @Builder.Default
    private int wrongStreak = 0;

    @Builder.Default
    private int correctCount = 0;

    @Builder.Default
    private int wrongCount = 0;

    private LocalDate nextReviewDate;
    private LocalDate lastStudiedDate;
}
