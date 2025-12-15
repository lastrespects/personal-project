package com.mmb.repository;

import com.mmb.entity.StudyRecord;
import com.mmb.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Integer> {

        // 오늘 특정 시간 구간 동안 공부한 단어 목록 (예: 오늘의 학습 단어)
        @Query("select distinct w " +
                        "from StudyRecord sr " +
                        "join Word w on w.id = sr.wordId " +
                        "where sr.memberId = :memberId " +
                        "and sr.studiedAt >= :start " +
                        "and sr.studiedAt < :end " +
                        "order by sr.studiedAt desc, sr.id desc")
        List<Word> findStudiedWordsBetween(@Param("memberId") Integer memberId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 특정 시점 이후에 공부한 단어들
        @Query("select distinct w " +
                        "from StudyRecord sr " +
                        "join Word w on w.id = sr.wordId " +
                        "where sr.memberId = :memberId " +
                        "and sr.studiedAt >= :since " +
                        "order by sr.studiedAt desc, sr.id desc")
        List<Word> findStudiedWordsSince(@Param("memberId") Integer memberId,
                        @Param("since") LocalDateTime since);

        // 최근에 공부한 단어들 (정렬 후 Word만 추출)
        @Query("select w " +
                        "from StudyRecord sr " +
                        "join Word w on w.id = sr.wordId " +
                        "where sr.memberId = :memberId " +
                        "order by sr.studiedAt desc")
        List<Word> findRecentStudiedWords(@Param("memberId") Integer memberId);

        // 오늘 퀴즈/책 학습 횟수 (studyType + 시간 구간)
        long countByMemberIdAndStudyTypeAndStudiedAtBetween(Integer memberId,
                        String studyType,
                        LocalDateTime start,
                        LocalDateTime end);

        // 오늘 퀴즈 정답 횟수 (Entity field: correct)
        long countByMemberIdAndStudyTypeAndCorrectAndStudiedAtBetween(Integer memberId,
                        String studyType,
                        boolean correct,
                        LocalDateTime start,
                        LocalDateTime end);

        @Query("""
                        select count(sr.id)
                        from StudyRecord sr
                        where sr.memberId = :memberId
                          and sr.studyType = 'QUIZ'
                          and sr.correct = true
                          and sr.studiedAt between :start and :end
                        """)
        long countQuizCorrect(@Param("memberId") Integer memberId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 메인 대시보드: 오늘 "학습한 단어 수" (단어 기준 distinct 개수)
        @Query("select count(distinct sr.wordId) " +
                        "from StudyRecord sr " +
                        "where sr.memberId = :memberId " +
                        "and sr.studiedAt >= :start " +
                        "and sr.studiedAt < :end")
        long countTodayLearnedWords(@Param("memberId") Integer memberId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 최근 학습 로그 100개
        List<StudyRecord> findTop100ByMemberIdOrderByStudiedAtDesc(Integer memberId);
}
