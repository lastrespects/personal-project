package com.mmb.repository;

import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

  // ✅ 오늘 학습한 단어(중복 제거)
  @Query("""
          select distinct sr.word
          from StudyRecord sr
          where sr.member.id = :memberId
            and sr.studiedAt >= :start
            and sr.studiedAt < :end
          order by sr.word.id desc
      """)
  List<Word> findStudiedWordsBetween(@Param("memberId") Long memberId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  // ✅ 최근 N일 학습 단어
  @Query("""
          select distinct sr.word
          from StudyRecord sr
          where sr.member.id = :memberId
            and sr.studiedAt >= :since
          order by sr.studiedAt desc
      """)
  List<Word> findStudiedWordsSince(@Param("memberId") Long memberId,
      @Param("since") LocalDateTime since);

  // ✅ 최근 학습 기록 쿼리 (보충용)
  @Query("""
          select distinct sr.word
          from StudyRecord sr
          where sr.member.id = :memberId
          order by sr.studiedAt desc
      """)
  List<Word> findRecentStudiedWords(@Param("memberId") Long memberId);

  // ✅ 오늘 학습한 단어 수 카운트
  @Query("""
          select count(distinct sr.word.id)
          from StudyRecord sr
          where sr.member.id = :memberId
            and sr.studiedAt >= :start
            and sr.studiedAt < :end
      """)
  long countTodayLearnedWords(@Param("memberId") Long memberId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
