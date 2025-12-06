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

    // login
    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100)
    private String email;

    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password;

    // profile
    @Column(length = 20, nullable = false)
    private String realName;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    private Integer age;

    @Column(length = 20)
    private String region;

    @Column(nullable = false)
    @Builder.Default
    private Integer dailyTarget = 30;

    @Column(nullable = false)
    @Builder.Default
    private Integer authLevel = 3; // 0=ADMIN, 3=USER

    // track nickname changes (for 30-day lock)
    private LocalDateTime nicknameUpdatedAt;

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
        if (this.authLevel == null) {
            this.authLevel = 3;
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
        return this.authLevel != null && this.authLevel == 0;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
