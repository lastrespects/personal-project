package com.mmb.repository;

import com.mmb.entity.DailyWordSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyWordSetRepository extends JpaRepository<DailyWordSet, Integer> {
    Optional<DailyWordSet> findByMemberIdAndStudyDate(Integer memberId, LocalDate studyDate);
}
