DROP DATABASE IF EXISTS MMB_DB;
CREATE DATABASE MMB_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE MMB_DB;

-- =========================================================
-- 1) MEMBER
-- =========================================================
CREATE TABLE `member` (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID',
  email VARCHAR(100) NOT NULL COMMENT '이메일',
  `PASSWORD` VARCHAR(255) NOT NULL COMMENT '비밀번호 해시 (bcrypt)',

  realName VARCHAR(20) NOT NULL COMMENT '실명',
  nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '닉네임',

  age INT UNSIGNED DEFAULT 0 COMMENT '나이',
  region VARCHAR(20) NULL COMMENT '거주지역',

  dailyTarget INT UNSIGNED NOT NULL DEFAULT 30 COMMENT '일일 학습 목표량',
  authLevel INT UNSIGNED NOT NULL DEFAULT 3 COMMENT '관리자=0, 사용자=3',

  nicknameUpdatedAt DATETIME NULL COMMENT '닉네임 변경 시각',
  deletedAt DATETIME NULL COMMENT '탈퇴 시각',
  restoreUntil DATETIME NULL COMMENT '복구 가능 기한'
);

-- =========================================================
-- 2) WORD
-- =========================================================
CREATE TABLE word (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  spelling VARCHAR(100) NOT NULL COMMENT '단어 철자',
  meaning VARCHAR(255) NOT NULL COMMENT '단어 뜻',
  exampleSentence TEXT NULL COMMENT '예문',
  audioPath VARCHAR(255) NULL COMMENT '오디오 경로',

  UNIQUE KEY uq_word_spelling (spelling)
);

-- =========================================================
-- 3) STUDY_RECORD
-- =========================================================
CREATE TABLE study_record (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  memberId INT UNSIGNED NOT NULL,
  wordId INT UNSIGNED NOT NULL,

  studiedAt DATETIME NOT NULL,
  correct TINYINT(1) NOT NULL DEFAULT 1,
  studyType VARCHAR(20) NOT NULL COMMENT 'BOOK, QUIZ 등',

  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_study_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE CASCADE,
  CONSTRAINT fk_study_word
    FOREIGN KEY (wordId) REFERENCES word(id) ON DELETE CASCADE,

  INDEX idx_study_member_time (memberId, studiedAt),
  INDEX idx_study_member_type_time (memberId, studyType, studiedAt)
);

-- =========================================================
-- 4) WORD_PROGRESS
-- =========================================================
CREATE TABLE word_progress (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  memberId INT UNSIGNED NOT NULL,
  wordId INT UNSIGNED NOT NULL,

  wrongStreak INT NOT NULL DEFAULT 0,
  correctCount INT NOT NULL DEFAULT 0,
  wrongCount INT NOT NULL DEFAULT 0,

  nextReviewDate DATE NULL,
  lastStudiedDate DATE NULL,

  UNIQUE KEY uq_member_word (memberId, wordId),

  CONSTRAINT fk_wp_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE CASCADE,
  CONSTRAINT fk_wp_word
    FOREIGN KEY (wordId) REFERENCES word(id) ON DELETE CASCADE,

  INDEX idx_wp_member_next (memberId, nextReviewDate)
);

-- =========================================================
-- 5) BOARD
-- =========================================================
CREATE TABLE board (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  boardName VARCHAR(20) NOT NULL
);

-- =========================================================
-- 6) ARTICLE
-- =========================================================
CREATE TABLE article (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  memberId INT UNSIGNED NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,

  boardId INT UNSIGNED NOT NULL,
  views INT UNSIGNED NOT NULL DEFAULT 0,

  CONSTRAINT fk_article_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE SET NULL,
  CONSTRAINT fk_article_board
    FOREIGN KEY (boardId) REFERENCES board(id) ON DELETE CASCADE,

  INDEX idx_article_board (boardId),
  INDEX idx_article_member (memberId)
);

-- =========================================================
-- 7) LIKE_POINT
-- =========================================================
CREATE TABLE likePoint (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  memberId INT UNSIGNED NOT NULL,
  relTypeCode VARCHAR(20) NOT NULL,
  relId INT UNSIGNED NOT NULL,

  UNIQUE KEY uq_like (memberId, relTypeCode, relId),

  CONSTRAINT fk_like_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE CASCADE,

  INDEX idx_like_rel (relTypeCode, relId)
);

-- =========================================================
-- 8) REPLY
-- =========================================================
CREATE TABLE reply (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  memberId INT UNSIGNED NULL,
  relTypeCode VARCHAR(20) NOT NULL,
  relId INT UNSIGNED NOT NULL,
  content VARCHAR(500) NOT NULL,

  CONSTRAINT fk_reply_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE SET NULL,

  INDEX idx_reply_rel (relTypeCode, relId),
  INDEX idx_reply_member (memberId)
);

-- =========================================================
-- 9) FILE
-- =========================================================
CREATE TABLE `file` (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  originName VARCHAR(50) NOT NULL,
  savedName VARCHAR(100) NOT NULL,
  savedPath VARCHAR(100) NOT NULL
);

-- =========================================================
-- 10) DAILY_WORD_SET (오늘 단어 세트 "고정")
-- =========================================================
CREATE TABLE daily_word_set (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  memberId INT UNSIGNED NOT NULL,
  studyDate DATE NOT NULL,
  targetCount INT UNSIGNED NOT NULL,

  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_member_date (memberId, studyDate),

  CONSTRAINT fk_dws_member
    FOREIGN KEY (memberId) REFERENCES `member`(id) ON DELETE CASCADE
);

-- =========================================================
-- 11) DAILY_WORD_ITEM (세트의 단어 목록 + 순서 + 출처)
--   ※ regDate만 둠 (updateDate 없음)
-- =========================================================
CREATE TABLE daily_word_item (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,

  setId INT UNSIGNED NOT NULL,
  wordId INT UNSIGNED NOT NULL,

  sourceCode VARCHAR(20) NOT NULL COMMENT 'REVIEW/TODAY/LAST7/GENERATED',
  sortOrder INT UNSIGNED NOT NULL,

  regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_set_word (setId, wordId),
  INDEX idx_set_order (setId, sortOrder),

  CONSTRAINT fk_dwi_set
    FOREIGN KEY (setId) REFERENCES daily_word_set(id) ON DELETE CASCADE,
  CONSTRAINT fk_dwi_word
    FOREIGN KEY (wordId) REFERENCES word(id) ON DELETE CASCADE
);

-- =========================================================
-- 12) SAMPLE MEMBERS (admin/test1/test2)
-- =========================================================
SET @BCRYPT_1234 = '$2a$10$ubB6XGHgK6hpobXi3pkck.xfuhjMh1xisU6u8qmYFtPwjn5.pNGki';

INSERT INTO `member` (username, email, `PASSWORD`, realName, nickname, age, region, dailyTarget, authLevel)
VALUES
('admin', 'admin@example.com', @BCRYPT_1234, '관리자', '마스터', 30, '서울', 100, 0),
('test1', 'test1@example.com', @BCRYPT_1234, '테스트사용자1', '테스터1', 25, '경기', 50, 3),
('test2', 'test2@example.com', @BCRYPT_1234, '테스트사용자2', '테스터2', 35, '부산', 30, 3);

-- =========================================================
-- 13) SAMPLE BOARDS / ARTICLES
-- =========================================================
INSERT INTO board (boardName) VALUES ('공지사항'), ('질문과 답변');

INSERT INTO article (memberId, title, content, boardId)
VALUES
(2, '제목1', '내용1', 2),
(1, '공지1', '공지 내용1', 1),
(3, '제목3', '내용3', 2);
