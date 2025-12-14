package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_word_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_set_word", columnNames = {"setId", "wordId"}),
        indexes = {
                @Index(name = "idx_set_order", columnList = "setId, sortOrder")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyWordItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ✅ SQL 컬럼명: setId
    @Column(name = "setId", nullable = false)
    private Integer setId;

    // ✅ SQL 컬럼명: wordId
    @Column(name = "wordId", nullable = false)
    private Integer wordId;

    // ✅ SQL 컬럼명: sourceCode
    @Column(name = "sourceCode", nullable = false, length = 20)
    private String sourceCode; // REVIEW / TODAY / LAST7 / GENERATED

    // ✅ SQL 컬럼명: sortOrder
    @Column(name = "sortOrder", nullable = false)
    private Integer sortOrder;

    // ✅ SQL 컬럼명: regDate
    @Column(name = "regDate", nullable = false)
    private LocalDateTime regDate;
}
