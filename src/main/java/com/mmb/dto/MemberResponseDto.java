package com.mmb.dto;

import com.mmb.domain.Member;
import lombok.*;

import java.time.LocalDate;

/**
 * 회원 응답용 DTO (서비스 → 컨트롤러 → 클라이언트)
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private int dailyTarget;
    private int characterLevel;
    private int currentExp;
    private LocalDate lastHintDate;

    public static MemberResponseDto from(Member m) {
        return MemberResponseDto.builder()
                .id(m.getId())
                .username(m.getUsername())
                .nickname(m.getNickname())
                .dailyTarget(m.getDailyTarget())
                .characterLevel(m.getCharacterLevel())
                .currentExp(m.getCurrentExp())
                .lastHintDate(m.getLastHintDate())
                .build();
    }
}
