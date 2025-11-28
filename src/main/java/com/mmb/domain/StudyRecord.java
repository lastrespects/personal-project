package com.mmb.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * StudyRecord 엔티티
 * - wrongCount로 오답 횟수를 카운트
 * - nextReviewDate로 복습일을 제어
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "study_record")
public class StudyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    private int reviewStep;
    private LocalDate nextReviewDate;
    private int wrongCount;
}
