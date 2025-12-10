-- Initialize database and seed minimal sample data for local development
DROP DATABASE IF EXISTS MMB_DB;
CREATE DATABASE MMB_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE MMB_DB;

-- Members (supports both JPA entity fields and legacy MyBatis columns)
CREATE TABLE member (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    PASSWORD VARCHAR(255) NOT NULL,
    realName VARCHAR(50) NOT NULL,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    age INT UNSIGNED DEFAULT 0,
    region VARCHAR(50),
    dailyTarget INT UNSIGNED NOT NULL DEFAULT 30,
    authLevel INT UNSIGNED NOT NULL DEFAULT 3,
    nicknameUpdatedAt DATETIME NULL,
    deletedAt DATETIME NULL,
    restoreUntil DATETIME NULL,
    -- legacy columns for MyBatis DAO
    loginId VARCHAR(50) UNIQUE,
    loginPw VARCHAR(255),
    name VARCHAR(100),
    characterLevel INT UNSIGNED DEFAULT 1,
    currentExp INT UNSIGNED DEFAULT 0
);

-- Boards
CREATE TABLE board (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    boardName VARCHAR(50) NOT NULL
);

-- Articles
CREATE TABLE article (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    boardId INT UNSIGNED NOT NULL,
    views INT UNSIGNED NOT NULL DEFAULT 0
);

-- Likes
CREATE TABLE likePoint (
    memberId BIGINT UNSIGNED NOT NULL,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL
);

-- Replies
CREATE TABLE reply (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId BIGINT UNSIGNED NOT NULL,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL,
    content VARCHAR(500) NOT NULL
);

-- Files
CREATE TABLE file (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    originName VARCHAR(100) NOT NULL,
    savedName VARCHAR(200) NOT NULL,
    savedPath VARCHAR(255) NOT NULL
);

-- Words
CREATE TABLE word (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    spelling VARCHAR(100) NOT NULL,
    meaning VARCHAR(255) NOT NULL,
    exampleSentence TEXT,
    audioPath VARCHAR(255)
);

-- Study records (Log)
CREATE TABLE study_record (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    memberId BIGINT UNSIGNED NOT NULL,
    wordId BIGINT UNSIGNED NOT NULL,
    studiedAt DATETIME NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT TRUE,
    studyType VARCHAR(50)
);

-- Word Progress (SRS State)
CREATE TABLE word_progress (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    memberId BIGINT UNSIGNED NOT NULL,
    wordId BIGINT UNSIGNED NOT NULL,
    wrongStreak INT NOT NULL DEFAULT 0,
    correctCount INT NOT NULL DEFAULT 0,
    wrongCount INT NOT NULL DEFAULT 0,
    nextReviewDate DATE NULL,
    lastStudiedDate DATE NULL,
    UNIQUE KEY uk_member_word (memberId, wordId)
);

-- Seed data
SET @PW_1234 = '$2a$10$ubB6XGHgK6hpobXi3pkck.xfuhjMh1xisU6u8qmYFtPwjn5.pNGki';

INSERT INTO member (regDate, updateDate, username, email, PASSWORD, realName, nickname, age, region, dailyTarget, authLevel, loginId, loginPw, name)
VALUES
    (NOW(), NOW(), 'admin', 'admin@example.com', @PW_1234, '관리자', '관리자', 30, '서울', 100, 0, 'admin', @PW_1234, '관리자'),
    (NOW(), NOW(), 'test1', 'test1@example.com', @PW_1234, '테스트1', '테스터1', 25, '경기', 50, 3, 'test1', @PW_1234, '테스트1'),
    (NOW(), NOW(), 'test2', 'test2@example.com', @PW_1234, '테스트2', '테스터2', 35, '부산', 30, 3, 'test2', @PW_1234, '테스트2');

INSERT INTO board (boardName) VALUES ('공지사항'), ('자유'), ('질문');

INSERT INTO article (regDate, updateDate, memberId, title, content, boardId, views) VALUES
    (NOW(), NOW(), 2, '안녕하세요', '첫 번째 게시글입니다.', 2, 0),
    (NOW(), NOW(), 1, '공지 1', '공지사항 예시', 1, 0),
    (NOW(), NOW(), 3, '질문 1', '질문 예시', 3, 0);

INSERT INTO word (regDate, updateDate, spelling, meaning, exampleSentence) VALUES
    (NOW(), NOW(), 'serene', '고요한', 'a serene lake in the morning'),
    (NOW(), NOW(), 'diligent', '부지런한', 'she is diligent with her studies'),
    (NOW(), NOW(), 'ephemeral', '순식간의', 'beauty can be ephemeral');

INSERT INTO study_record (memberId, wordId, studiedAt, correct, studyType)
VALUES
    (2, 1, NOW(), true, 'QUIZ'),
    (2, 2, NOW(), false, 'QUIZ'),
    (3, 3, NOW(), true, 'QUIZ');
