-- SRS 필드 추가 및 기존 컬럼 정합성 맞추기
-- 실행 대상: 현재 study_record 테이블에 correctStreak/easeFactor/intervalDays/nextReviewAt가 없는 경우

ALTER TABLE study_record
    ADD COLUMN correctStreak INT NOT NULL DEFAULT 0 AFTER incorrectCount,
    ADD COLUMN easeFactor DOUBLE NOT NULL DEFAULT 2.5 AFTER correctStreak,
    ADD COLUMN intervalDays INT NOT NULL DEFAULT 0 AFTER easeFactor,
    ADD COLUMN nextReviewAt DATETIME NULL AFTER lastReviewDate;

-- 최초값 보정: 다음 복습시점이 비어있으면 lastReviewDate 또는 현재 시각으로 채움
UPDATE study_record
SET
    correctStreak = 0,
    easeFactor = 2.5,
    intervalDays = 0,
    nextReviewAt = COALESCE(lastReviewDate, NOW())
WHERE nextReviewAt IS NULL;

-- (선택) 회원-단어 유니크 제약 추가
-- ALTER TABLE study_record ADD CONSTRAINT uk_member_word UNIQUE (memberId, wordId);
