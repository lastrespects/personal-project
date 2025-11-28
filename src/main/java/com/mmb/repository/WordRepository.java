// WordRepository.java
package com.mmb.repository;

import com.mmb.domain.Word; // 패키지 변경
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    Optional<Word> findBySpelling(String spelling);
}