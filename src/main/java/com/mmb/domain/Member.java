package com.mmb.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // 로그인 ID
    private String password;
    private String nickname;

    // --- 학습 설정 ---
    private int dailyTarget; // 하루 목표 (30, 50, 100...)

    // --- 게임 요소 (캐릭터) ---
    private int characterLevel; // 캐릭터 레벨 (1:알, 2:아기, 3:어른)
    private int currentExp;     // 현재 경험치

    // --- 힌트 기능 (하루 1회 제한) ---
    private LocalDate lastHintDate; // 마지막으로 힌트 쓴 날짜

    public Member(String username, String nickname, int dailyTarget) {
        this.username = username;
        this.nickname = nickname;
        this.dailyTarget = dailyTarget;
        this.characterLevel = 1; // 1단계부터 시작
        this.currentExp = 0;
    }
    
    // 경험치 획득 로직
    public void gainExp(int amount) {
        this.currentExp += amount;
        // 레벨업 로직 (예: 100점마다 레벨업)
        if (this.currentExp >= 100 * this.characterLevel) {
            this.currentExp = 0;
            this.characterLevel++;
        }
    }
}