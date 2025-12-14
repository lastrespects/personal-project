package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_word_set",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_date", columnNames = {"memberId", "studyDate"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyWordSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ✅ SQL 컬럼명: memberId
    @Column(name = "memberId", nullable = false)
    private Integer memberId;

    // ✅ SQL 컬럼명: studyDate
    @Column(name = "studyDate", nullable = false)
    private LocalDate studyDate;

    // ✅ SQL 컬럼명: targetCount
    @Column(name = "targetCount", nullable = false)
    private Integer targetCount;

    // ✅ SQL 컬럼명: regDate
    @Column(name = "regDate", nullable = false)
    private LocalDateTime regDate;

    // ✅ SQL 컬럼명: updateDate
    @Column(name = "updateDate", nullable = false)
    private LocalDateTime updateDate;
}
