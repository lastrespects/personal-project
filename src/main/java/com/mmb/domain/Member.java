package com.mmb.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Member 엔티티
 * - builder 사용, lastHintDate는 LocalDate로 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member")
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String nickname;
    private int dailyTarget;

    @Builder.Default
    private int characterLevel = 1;

    @Builder.Default
    private int currentExp = 0;

    private LocalDate lastHintDate;

    public void gainExp(int exp) {
        this.currentExp += exp;
        if (this.currentExp >= 100) {
            this.characterLevel++;
            this.currentExp -= 100;
        }
    }
}
