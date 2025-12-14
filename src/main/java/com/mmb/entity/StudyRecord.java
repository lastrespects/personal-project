package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "study_record",
       indexes = {
           @Index(name = "idx_study_member_time", columnList = "memberId,studiedAt"),
           @Index(name = "idx_study_member_type_time", columnList = "memberId,studyType,studiedAt")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyRecord {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ INT PK

    // ✅ DB 컬럼명이 memberId / wordId (스네이크 아님)
    @Column(name = "memberId", nullable = false)
    private Integer memberId;

    @Column(name = "wordId", nullable = false)
    private Integer wordId;

    // FK 연결(읽기용). insert/update는 memberId/wordId로만 관리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", referencedColumnName = "id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wordId", referencedColumnName = "id", insertable = false, updatable = false)
    private Word word;

    @Column(nullable = false)
    private LocalDateTime studiedAt;

    @Column(nullable = false, length = 20)
    private String studyType;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(nullable = false)
    private LocalDateTime updateDate;

    @Transient
    public String getStudiedAtDisplay() {
        if (this.studiedAt == null) {
            return "";
        }
        return DISPLAY_FORMATTER.format(this.studiedAt);
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.regDate = now;
        this.updateDate = now;

        if (this.studiedAt == null) this.studiedAt = now;
        if (this.studyType == null) this.studyType = "BOOK";
        // boolean 기본값은 false라서, DB default(1) 기대면 안 됨 → 기본 true로 쓰고싶으면 여기서 처리
        // if (this.correct == false) ... (원하는 정책에 맞춰)
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
