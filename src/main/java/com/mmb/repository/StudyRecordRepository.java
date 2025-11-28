package com.mmb.repository;

import com.mmb.domain.StudyRecord;
import com.mmb.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    // `member_id`를 기준으로 조회하도록 native query 사용
    @Query(value = "SELECT * FROM study_record WHERE member_id = :memberId AND next_review_date <= :date", nativeQuery = true)
    List<StudyRecord> findTodayReviews(@Param("memberId") int memberId, @Param("date") LocalDate date);

    // `member_id`와 Word 객체로 존재 여부 확인
    boolean existsByMemberIdAndWord(int memberId, Word word);

    // JPA 메서드에서 memberId를 인자로 받도록 수정
    Optional<StudyRecord> findById(Long id);
}