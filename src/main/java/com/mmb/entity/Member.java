package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ INT PK

    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(nullable = false)
    private LocalDateTime updateDate;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    // DB 컬럼명이 PASSWORD(대문자)라서 명시적으로 매핑
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String realName;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    private Integer age;

    @Column(length = 20)
    private String region;

    // DB에 NOT NULL이 없어서 nullable 강제하지 않음(validate 일치)
    private Integer dailyTarget;

    @Column(nullable = false)
    private Integer authLevel;

    private LocalDateTime nicknameUpdatedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime restoreUntil;

    // ---- DB 컬럼에는 없으니 validate 통과용으로 Transient 유지 가능 ----
    @Transient
    @Builder.Default
    private Integer characterLevel = 1;

    @Transient
    @Builder.Default
    private Integer currentExp = 0;

    @Transient
    private String lastHintDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.regDate = now;
        this.updateDate = now;

        if (this.authLevel == null) this.authLevel = 3;
        if (this.dailyTarget == null) this.dailyTarget = 30;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return this.authLevel != null && this.authLevel == 0;
    }
}
