package com.mmb.repository;

import com.mmb.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

// Word 엔티티를 관리하며, 단어 중복 체크 기능을 제공합니다.
public interface WordRepository extends JpaRepository<Word, Long> {
    
    // 단어 중복 체크
    boolean existsBySpelling(String spelling);
}