package com.mmb.repository;

import com.mmb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {

    boolean existsBySpelling(String spelling);
}
