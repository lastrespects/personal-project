package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member") // 실제 테이블명이 member 라고 가정
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

    // loginId 컬럼과 매핑 (MyBatis 쪽과 호환)
    @Column(name = "loginId", unique = true, nullable = false)
    private String username;

    // loginPw 컬럼과 매핑
    @Column(name = "loginPw", nullable = false)
    private String password;

    // name 컬럼과 매핑
    @Column(name = "name", nullable = false)
    private String realName;

    @Column(unique = true, nullable = false)
    private String nickname;

    private Integer age;
    private String region;

    // 하루 목표 단어 수
    private Integer dailyTarget;

    // 권한 레벨 (예: 1=관리자, 3=일반회원 등)
    private Integer authLevel;

    // 게임 요소: 캐릭터 레벨/경험치/마지막 힌트 사용일
    @Builder.Default
    private Integer characterLevel = 1;

    @Builder.Default
    private Integer currentExp = 0;

    private String lastHintDate;

    // ====== 라이프사이클 콜백 ======

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.regDate = now;
        this.updateDate = now;

        if (this.dailyTarget == null) {
            this.dailyTarget = 30;   // 기본 하루 목표
        }
        if (this.authLevel == null) {
            this.authLevel = 3;      // 기본 권한 레벨(예: 일반 회원)
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
        if (exp <= 0) return;

        if (this.currentExp == null) this.currentExp = 0;
        if (this.characterLevel == null) this.characterLevel = 1;

        this.currentExp += exp;
        while (this.currentExp >= 100) {
            this.characterLevel++;
            this.currentExp -= 100;
        }
    }
}
