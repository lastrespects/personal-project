# 1. 데이터베이스 초기화
DROP DATABASE IF EXISTS MMB_DB;
CREATE DATABASE MMB_DB;
USE MMB_DB;

# 2. 단어/학습 기록 테이블
CREATE TABLE word (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    spelling VARCHAR(100) NOT NULL COMMENT '단어 철자',
    meaning VARCHAR(255) NOT NULL COMMENT '단어 의미',
    exampleSentence TEXT COMMENT '예문',
    audioPath VARCHAR(255) COMMENT 'TTS 오디오 파일 경로'
);

CREATE TABLE study_record (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    memberId INT UNSIGNED NOT NULL COMMENT '회원 ID',
    wordId INT UNSIGNED NOT NULL COMMENT '학습할 단어 ID',
    correctCount INT NOT NULL DEFAULT 0 COMMENT '정답 횟수',
    incorrectCount INT NOT NULL DEFAULT 0 COMMENT '오답 횟수',
    totalAttempts INT NOT NULL DEFAULT 0 COMMENT '총 시도 횟수',
    lastReviewDate DATETIME COMMENT '마지막 복습 시각'
);

# 3. 게시글/회원/댓글 등 기본 테이블
CREATE TABLE article(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId INT UNSIGNED NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    boardId INT UNSIGNED NOT NULL,
    views INT UNSIGNED NOT NULL DEFAULT 0
);

CREATE TABLE `member`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID',
    email VARCHAR(100) NOT NULL COMMENT '이메일',
    PASSWORD VARCHAR(255) NOT NULL COMMENT 'BCrypt 암호화된 비밀번호',
    realName VARCHAR(20) NOT NULL COMMENT '실명',
    nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '사용자 닉네임 (유니크)',
    age INT UNSIGNED DEFAULT 0 COMMENT '나이',
    region VARCHAR(20) COMMENT '거주지역',
    dailyTarget INT UNSIGNED DEFAULT 30 COMMENT '일일 학습 목표량',
    authLevel INT UNSIGNED NOT NULL DEFAULT 3 COMMENT '관리자 = 0, 사용자 = 3',
    nicknameUpdatedAt DATETIME NULL COMMENT '최근 닉네임 변경일 (30일 제한)'
);

CREATE TABLE board(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    boardName VARCHAR(20) NOT NULL
);

CREATE TABLE likePoint(
    memberId INT UNSIGNED NOT NULL,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL
);

CREATE TABLE reply(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId INT UNSIGNED NOT NULL,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL,
    content VARCHAR(500) NOT NULL
);

CREATE TABLE `file`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    originName VARCHAR(50) NOT NULL,
    savedName VARCHAR(100) NOT NULL,
    savedPath VARCHAR(100) NOT NULL
);

# 4. 초기 데이터
-- BCrypt 암호화된 '1234'
SET @BCRYPT_PASSWORD_1234 = '$2a$10$wTfH8y/w5QG7lYh.Q8LzG.8uN/tTj9dYd3uB9tZ2A9D4X7M7O5f9';

INSERT INTO `member` (regDate, updateDate, username, email, PASSWORD, realName, nickname, age, region, dailyTarget, authLevel, nicknameUpdatedAt)
VALUES (NOW(), NOW(), 'admin', 'admin@example.com', @BCRYPT_PASSWORD_1234, '관리자', '마스터', 30, '서울', 100, 0, NULL);

INSERT INTO `member` (regDate, updateDate, username, email, PASSWORD, realName, nickname, age, region, dailyTarget, authLevel, nicknameUpdatedAt)
VALUES (NOW(), NOW(), 'test1', 'test1@example.com', @BCRYPT_PASSWORD_1234, '테스트사용자1', '테스트1', 25, '경기', 50, 3, NULL);

INSERT INTO `member` (regDate, updateDate, username, email, PASSWORD, realName, nickname, age, region, dailyTarget, authLevel, nicknameUpdatedAt)
VALUES (NOW(), NOW(), 'test2', 'test2@example.com', @BCRYPT_PASSWORD_1234, '테스트사용자2', '테스트2', 35, '부산', 30, 3, NULL);

INSERT INTO board (boardName) VALUES ('공지사항'), ('자유'), ('질문');

INSERT INTO article (regDate, updateDate, memberId, title, content, boardId)
VALUES
    (NOW(), NOW(), 2, '제목1', '내용1', 2),
    (NOW(), NOW(), 1, '공지1', '공지 내용1', 1),
    (NOW(), NOW(), 3, '제목3', '내용3', 3);
