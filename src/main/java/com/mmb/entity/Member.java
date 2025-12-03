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

    // 등록/수정 일시
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    @Column(nullable = false)
    private LocalDateTime updateDate;

    // DB 컬럼: username VARCHAR(50) UNIQUE NOT NULL
    @Column(length = 50, nullable = false, unique = true)
    private String username; // 로그인 ID

    // DB 컬럼: PASSWORD VARCHAR(255) NOT NULL (대문자 주의)
    @Column(name = "PASSWORD", length = 255, nullable = false)
    private String password; // BCrypt 암호화된 비밀번호

    // DB 컬럼: realName VARCHAR(20) NOT NULL
    @Column(length = 20, nullable = false)
    private String realName; // 실명

    // DB 컬럼: nickname VARCHAR(50) UNIQUE NOT NULL
    @Column(length = 50, nullable = false, unique = true)
    private String nickname; // 닉네임

    // DB 컬럼: age INT UNSIGNED DEFAULT 0
    private Integer age;

    // DB 컬럼: region VARCHAR(20)
    @Column(length = 20)
    private String region;

    // DB 컬럼: dailyTarget INT UNSIGNED DEFAULT 30
    @Column(nullable = false)
    @Builder.Default
    private Integer dailyTarget = 30; // 하루 목표 단어 수

    // DB 컬럼: authLevel INT UNSIGNED NOT NULL DEFAULT 3
    @Column(nullable = false)
    @Builder.Default
    private Integer authLevel = 3; // 권한 레벨 (0=관리자, 3=일반)

    // ====== 게임 요소: 아직 DB 컬럼 없음 → @Transient 로 메모리에서만 사용 ======

    @Transient
    @Builder.Default
    private Integer characterLevel = 1;

    @Transient
    @Builder.Default
    private Integer currentExp = 0;

    @Transient
    private String lastHintDate;

    // ====== 라이프사이클 콜백 ======

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

    // ====== 경험치 획득 메서드 ======
    public void gainExp(int exp) {
        if (exp <= 0)
            return;

        if (this.currentExp == null)
            this.currentExp = 0;
        if (this.characterLevel == null)
            this.characterLevel = 1;

        this.currentExp += exp;
        while (this.currentExp >= 100) {
            this.characterLevel++;
            this.currentExp -= 100;
        }
    }

    // 관리자 체크 편의 메서드
    public boolean isAdmin() {
        return this.authLevel != null && this.authLevel == 0;
    }
}
