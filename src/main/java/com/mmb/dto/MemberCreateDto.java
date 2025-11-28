package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 생성 요청용 DTO (컨트롤러 → 서비스)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateDto {
    private String username;
    private String password;
    private String nickname;
    private int dailyTarget;
}
