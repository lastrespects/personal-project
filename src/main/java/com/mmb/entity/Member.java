package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // created/updated timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(nullable = false)
    private LocalDateTime updateDate;

    // 로그인 아이디
    @Column(unique = true, nullable = false)
    private String username;

    // 암호화된 비밀번호
    @Column(nullable = false, length = 255)
    private String password;

    // 권한 레벨 (1: 일반, 7: 관리자 등)
    @Builder.Default
    private int authLevel = 3; // 0=ADMIN, 3=USER

    // 이메일
    private String email;

    // profile
    @Column(length = 20, nullable = false)
    private String realName;

    // 닉네임 + 닉네임 마지막 변경 시각
    @Column(length = 50, nullable = false)
    private String nickname;

    private LocalDateTime nicknameUpdatedAt;

    private Integer age;

    // 지역 / 일일 학습 목표
    private String region;

    @Builder.Default
    private Integer dailyTarget = 30;

    // soft delete (keep for 7 days)
    private LocalDateTime deletedAt;
    private LocalDateTime restoreUntil;

    // ====== game-like transient fields ======
    @Transient
    @Builder.Default
    private Integer characterLevel = 1;

    @Transient
    @Builder.Default
    private Integer currentExp = 0;

    @Transient
    private String lastHintDate;

    // lifecycle
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.regDate = now;
        this.updateDate = now;

        if (this.dailyTarget == null) {
            this.dailyTarget = 30;
        }
        if (this.characterLevel == null) {
            this.characterLevel = 1;
        }
        if (this.currentExp == null) {
            this.currentExp = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    // exp gain helper
    public void gainExp(int exp) {
        if (exp <= 0) return;

        if (this.currentExp == null) this.currentExp = 0;
        if (this.characterLevel == null) this.characterLevel = 1;

        this.currentExp += exp;
        while (this.currentExp >= 100) {
            this.characterLevel++;
            this.currentExp -= 100;
        }
    }

    public boolean isAdmin() {
        return this.authLevel == 0;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
