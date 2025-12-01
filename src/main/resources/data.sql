# 1. 데이터베이스 초기화
DROP DATABASE IF EXISTS MMB_DB; -- SB_AM -> MMB_DB
CREATE DATABASE MMB_DB;        -- SB_AM -> MMB_DB
USE MMB_DB;                    -- SB_AM -> MMB_DB

# =========================================================
# 2. 핵심 서비스 테이블 (AI 단어 학습)
# =========================================================

# 오늘의 단어 목록을 저장하는 테이블
CREATE TABLE word (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , spelling VARCHAR(100) NOT NULL COMMENT '단어 철자'
    , meaning VARCHAR(255) NOT NULL COMMENT '단어 뜻'
    , exampleSentence TEXT COMMENT '예문'
    , audioPath VARCHAR(255) COMMENT 'TTS 오디오 파일 경로'
);

# 회원의 학습 기록 및 통계를 저장하는 테이블
CREATE TABLE study_record (
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , memberId INT UNSIGNED NOT NULL COMMENT '회원 ID'
    , wordId INT UNSIGNED NOT NULL COMMENT '학습한 단어 ID'
    , correctCount INT NOT NULL DEFAULT 0 COMMENT '정답 횟수'
    , incorrectCount INT NOT NULL DEFAULT 0 COMMENT '오답 횟수'
    , totalAttempts INT NOT NULL DEFAULT 0 COMMENT '총 시도 횟수'
    , lastReviewDate DATETIME COMMENT '마지막 학습 일시'
);

# =========================================================
# 3. 기존 강사님 제공 테이블 (게시글, 회원, 게시판 등)
# =========================================================

# ARTICLE 테이블 (게시글)
CREATE TABLE article(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , memberId INT UNSIGNED NOT NULL
    , title VARCHAR(100) NOT NULL
    , content TEXT NOT NULL
    , boardId INT UNSIGNED NOT NULL
    , views INT UNSIGNED NOT NULL DEFAULT 0
);

# MEMBER 테이블 (수정됨: 닉네임, 나이, 지역, 학습량 추가)
CREATE TABLE `member`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID'
    , PASSWORD VARCHAR(255) NOT NULL COMMENT 'BCrypt 암호화된 비밀번호'
    , realName VARCHAR(20) NOT NULL COMMENT '실명'
    , nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '사용자 닉네임 (유니크)'
    , age INT UNSIGNED DEFAULT 0 COMMENT '나이'
    , region VARCHAR(20) COMMENT '거주지역'
    , dailyTarget INT UNSIGNED DEFAULT 30 COMMENT '일일 학습 목표량'
    , authLevel INT UNSIGNED NOT NULL DEFAULT 3 COMMENT '관리자 = 0, 사용자 = 3'
);

# BOARD 테이블 (게시판 정보)
CREATE TABLE board(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , boardName VARCHAR(20) NOT NULL
);

# LIKE_POINT 테이블 (좋아요)
CREATE TABLE likePoint(
    memberId INT UNSIGNED NOT NULL
    , relTypeCode VARCHAR(20) NOT NULL
    , relId INT UNSIGNED NOT NULL
);

# REPLY 테이블 (댓글)
CREATE TABLE reply(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , memberId INT UNSIGNED NOT NULL
    , relTypeCode VARCHAR(20) NOT NULL
    , relId INT UNSIGNED NOT NULL
    , content VARCHAR(500) NOT NULL
);

# FILE 테이블 (파일 업로드)
CREATE TABLE `file`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , originName VARCHAR(50) NOT NULL
    , savedName VARCHAR(100) NOT NULL
    , savedPath VARCHAR(100) NOT NULL
);

# =========================================================
# 4. 초기 데이터 삽입 (테스트용)
# =========================================================

-- BCrypt 암호화된 '1234' 비밀번호
SET @BCRYPT_PASSWORD_1234 = '$2a$10$wTfH8y/w5QG7lYh.Q8LzG.8uN/tTj9dYd3uB9tZ2A9D4X7M7O5f9';

INSERT INTO `member`
    SET regDate = NOW()
        , updateDate = NOW()
        , username = 'admin'
        , PASSWORD = @BCRYPT_PASSWORD_1234
        , realName = '관리자'
        , nickname = '마스터'
        , age = 30
        , region = '서울'
        , dailyTarget = 100
        , authLevel = 0;

INSERT INTO `member`
    SET regDate = NOW()
        , updateDate = NOW()
        , username = 'test1'
        , PASSWORD = @BCRYPT_PASSWORD_1234
        , realName = '테스트사용자1'
        , nickname = '테스터1'
        , age = 25
        , region = '경기'
        , dailyTarget = 50
        , authLevel = 3;
        
INSERT INTO `member`
    SET regDate = NOW()
        , updateDate = NOW()
        , username = 'test2'
        , PASSWORD = @BCRYPT_PASSWORD_1234
        , realName = '테스트사용자2'
        , nickname = '테스터2'
        , age = 35
        , region = '부산'
        , dailyTarget = 30
        , authLevel = 3;
        
-- 게시판 초기 데이터 (유지)
INSERT INTO board
    SET boardName = '공지사항';
INSERT INTO board
    SET boardName = '자유';
INSERT INTO board
    SET boardName = '질문과 답변';

-- 게시글 초기 데이터 (memberId는 새 ID에 맞춰 조정될 수 있음. 일단 유지)
INSERT INTO article
    SET regDate = NOW(), updateDate = NOW(), memberId = 2, title = '제목1', content = '내용1', boardId = 2;
INSERT INTO article
    SET regDate = NOW(), updateDate = NOW(), memberId = 1, title = '공지1', content = '공지 내용1', boardId = 1;
INSERT INTO article
    SET regDate = NOW(), updateDate = NOW(), memberId = 3, title = '제목3', content = '내용3', boardId = 3;