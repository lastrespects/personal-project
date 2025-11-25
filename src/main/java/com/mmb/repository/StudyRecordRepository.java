package com.mmb.repository;

import com.mmb.domain.StudyRecord;
import com.mmb.domain.Member;
import com.mmb.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

// StudyRecord 엔티티를 관리하며, 망각곡선 로직의 핵심 쿼리를 제공합니다.
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    
    // 오늘 복습해야 할 단어 목록 조회 (핵심 쿼리)
    @Query("SELECT r FROM StudyRecord r WHERE r.member.id = :memberId AND r.nextReviewDate <= :today")
    List<StudyRecord> findTodayReviews(@Param("memberId") Long memberId, @Param("today") LocalDate today);
    
    // 해당 사용자가 특정 단어를 이미 학습 중인지 확인
    boolean existsByMemberAndWord(Member member, Word word);
}