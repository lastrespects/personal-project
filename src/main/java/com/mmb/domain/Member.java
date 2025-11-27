package com.mmb.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor // JSON 파싱을 위해 필수
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password; // 실제 앱이라면 암호화해야 함

    private String nickname;
    
    // 학습 목표
    private int dailyTarget; 

    // 캐릭터 레벨 및 경험치
    private int characterLevel;
    private int currentExp;
    
    // 힌트 사용 기록
    private LocalDate lastHintDate;

    // 경험치 증가 메서드
    public void gainExp(int exp) {
        this.currentExp += exp;
        // 100점당 레벨업하는 간단한 로직 (예시)
        if (this.currentExp >= 100) {
            this.characterLevel++;
            this.currentExp -= 100;
        }
    }
}