package com.mmb.repository;

import com.mmb.domain.StudyRecord;
import com.mmb.domain.Member;
import com.mmb.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    @Query("SELECT r FROM StudyRecord r WHERE r.member.id = :memberId AND r.nextReviewDate <= :today")
    List<StudyRecord> findTodayReviews(@Param("memberId") Long memberId, @Param("today") LocalDate today);

    boolean existsByMemberAndWord(Member member, Word word);
}
