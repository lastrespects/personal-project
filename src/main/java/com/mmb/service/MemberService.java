// MemberService.java
package com.mmb.service;

import org.springframework.stereotype.Service;

import com.mmb.dao.MemberDao; // 패키지 변경
import com.mmb.dto.Member; // 패키지 변경

@Service
public class MemberService {

	private MemberDao memberDao;
	
	public MemberService(MemberDao memberDao) {
		this.memberDao = memberDao;
	}

	public Member getMemberByLoginId(String loginId) {
		return this.memberDao.getMemberByLoginId(loginId);
	}
    
    // FullLearningService를 위해 추가
    public Member getMemberById(int id) {
        return this.memberDao.getMemberById(id);
    }

	public void joinMember(String loginId, String loginPw, String name) {
		this.memberDao.joinMember(loginId, loginPw, name);
	}

	public String getLoginId(int id) {
		return this.memberDao.getLoginId(id);
	}
	
}