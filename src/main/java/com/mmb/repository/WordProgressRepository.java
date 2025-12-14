package com.mmb.repository;

import com.mmb.entity.WordProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WordProgressRepository extends JpaRepository<WordProgress, Integer> {

    Optional<WordProgress> findByMemberIdAndWordId(Integer memberId, Integer wordId);

    List<WordProgress> findByMemberIdAndNextReviewDateLessThanEqual(Integer memberId, LocalDate date);
}
