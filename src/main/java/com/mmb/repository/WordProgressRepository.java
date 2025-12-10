package com.mmb.repository;

import com.mmb.entity.WordProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordProgressRepository extends JpaRepository<WordProgress, Long> {

    Optional<WordProgress> findByMemberIdAndWordId(Long memberId, Long wordId);

    List<WordProgress> findByMemberIdAndNextReviewDateLessThanEqual(Long memberId, LocalDate date);
}
