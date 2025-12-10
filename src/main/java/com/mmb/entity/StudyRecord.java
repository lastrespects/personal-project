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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wordId")
    private Word word;

    // ✅ 학습 시각
    private LocalDateTime studiedAt;

    // ✅ 정답 여부
    @Builder.Default
    private boolean correct = true;

    // ✅ 학습 타입 (e.g., "BOOK", "QUIZ", "TODAY_WORD")
    private String studyType;
}
