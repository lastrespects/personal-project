package com.mmb.repository;

import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    // 오늘 복습할 문제들
    @Query(value = "SELECT * FROM study_record WHERE memberId = :memberId AND lastReviewDate <= :date",
           nativeQuery = true)
    List<StudyRecord> findTodayReviews(@Param("memberId") int memberId,
                                       @Param("date") LocalDate date);

    boolean existsByMemberIdAndWord(int memberId, Word word);

    // 회원 + 단어를 한 번에 가져오기
    @Query("SELECT sr FROM StudyRecord sr JOIN FETCH sr.word WHERE sr.memberId = :memberId")
    List<StudyRecord> findByMemberIdWithWord(@Param("memberId") Long memberId);
}
