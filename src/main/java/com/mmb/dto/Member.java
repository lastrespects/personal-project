package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	private int id;
	private String regDate;
	private String updateDate;
	private String loginId;
	private String loginPw;
	private String name;
	private int authLevel;
    
    // [추가] 학습 관련 필드
    private int dailyTarget;
    private int characterLevel;
    private int currentExp;
}