// src/main/java/com/mmb/repository/WordRepository.java
package com.mmb.repository;

import com.mmb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> findFirstBySpellingIgnoreCase(String spelling);

    boolean existsBySpelling(String spelling);
}
