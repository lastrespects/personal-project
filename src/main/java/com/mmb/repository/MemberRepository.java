package com.mmb.repository;

import com.mmb.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // username으로 회원을 조회하는 메서드 (로그인/가입 시 사용)
    Member findByUsername(String username);
}