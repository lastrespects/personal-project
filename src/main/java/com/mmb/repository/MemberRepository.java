// src/main/java/com/mmb/repository/MemberRepository.java
package com.mmb.repository;

import com.mmb.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);

    // 추가
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
}
