// src/main/java/com/mmb/repository/StudyRecordRepository.java
package com.mmb.repository;

import com.mmb.entity.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    // memberId로 조회 (JPQL)
    @Query("select s from StudyRecord s where s.member.id = :memberId")
    List<StudyRecord> findByMemberId(@Param("memberId") Long memberId);

    // memberId + wordId로 조회
    @Query("select s from StudyRecord s where s.member.id = :memberId and s.word.id = :wordId")
    Optional<StudyRecord> findByMemberIdAndWordId(@Param("memberId") Long memberId,
                                                  @Param("wordId") Long wordId);

    // 오늘 복습 기록 (FullLearningService에서 사용)
    @Query("""
           select s
           from StudyRecord s
           where s.member.id = :memberId
             and function('date', s.lastReviewDate) = :date
           """)
    List<StudyRecord> findTodayReviews(@Param("memberId") Long memberId,
                                       @Param("date") LocalDate date);

    // member + word를 한 번에 fetch (WordService에서 사용)
    @Query("""
           select s
           from StudyRecord s
           join fetch s.word
           where s.member.id = :memberId
           """)
    List<StudyRecord> findByMemberIdWithWord(@Param("memberId") Long memberId);
}
