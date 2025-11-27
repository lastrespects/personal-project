package com.mmb.repository;

import com.mmb.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
    
    // 단어가 이미 DB에 존재하는지 확인 (true/false)
    boolean existsBySpelling(String spelling);

    // ★ 추가됨: 단어 스펠링으로 단어 객체 찾아오기 (ID 조회용)
    Word findBySpelling(String spelling);
}