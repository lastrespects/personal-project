package com.mmb.repository;

import com.mmb.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Member 엔티티를 관리하며, 기본 CRUD 기능을 제공하고, 랭킹 조회를 담당합니다.
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 랭킹 조회 (경험치 높은 순 Top 10)
    List<Member> findTop10ByOrderByCurrentExpDesc();
}