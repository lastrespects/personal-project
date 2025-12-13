package com.mmb.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", insertable = false, updatable = false)
    private Word word;

    private LocalDateTime studiedAt;

    private String studyType;

    private boolean correct;

    private LocalDate studyDate;

    @Transient
    private String studiedAtDisplay;

    public String getStudiedAtDisplay() {
        if (studiedAtDisplay == null && studiedAt != null) {
            studiedAtDisplay = studiedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd일 HH:mm:ss"));
        }
        return studiedAtDisplay;
    }

    public void setStudiedAtDisplay(String studiedAtDisplay) {
        this.studiedAtDisplay = studiedAtDisplay;
    }
}
