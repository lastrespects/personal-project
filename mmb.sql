DROP DATABASE IF EXISTS MMB_DB;
CREATE DATABASE MMB_DB;
USE MMB_DB;



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
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  regDate DATETIME NOT NULL,
  memberId INT UNSIGNED NOT NULL COMMENT '회원 ID',
  wordId INT UNSIGNED NOT NULL COMMENT '단어 ID',
  totalAttempts INT UNSIGNED NOT NULL DEFAULT 0,
  correctCount INT UNSIGNED NOT NULL DEFAULT 0,
  incorrectCount INT UNSIGNED NOT NULL DEFAULT 0,
  lastReviewDate DATETIME NULL,
  FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE CASCADE,
  FOREIGN KEY (wordId) REFERENCES word(id) ON DELETE CASCADE
);


# MEMBER 테이블 (수정 반영: email / 닉변 시각 / 탈퇴 관련 컬럼 포함)
CREATE TABLE `member`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID'
    , email VARCHAR(100) NOT NULL COMMENT '이메일'
    , PASSWORD VARCHAR(255) NOT NULL COMMENT '비밀번호 해시 (bcrypt)'
    , realName VARCHAR(20) NOT NULL COMMENT '실명'
    , nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '사용자 닉네임 (유니크)'
    , age INT UNSIGNED DEFAULT 0 COMMENT '나이'
    , region VARCHAR(20) COMMENT '거주지역'
    , dailyTarget INT UNSIGNED DEFAULT 30 COMMENT '일일 학습 목표량'
    , authLevel INT UNSIGNED NOT NULL DEFAULT 3 COMMENT '관리자 = 0, 사용자 = 3'
    , nicknameUpdatedAt DATETIME NULL COMMENT '닉네임 변경 시각'
    , deletedAt DATETIME NULL COMMENT '탈퇴 시각'
    , restoreUntil DATETIME NULL COMMENT '복구 가능 기한'
);

# BOARD 테이블 (게시판 정보)
CREATE TABLE board(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , boardName VARCHAR(20) NOT NULL
);

# ARTICLE 테이블 (게시글) - 회원 탈퇴 시 게시글은 남음 (memberId SET NULL)
CREATE TABLE article(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId INT UNSIGNED,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    boardId INT UNSIGNED NOT NULL,
    views INT UNSIGNED NOT NULL DEFAULT 0,
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE SET NULL,
    FOREIGN KEY (boardId) REFERENCES board(id) ON DELETE CASCADE
);

# LIKE_POINT 테이블 (좋아요) - 회원 탈퇴 시 좋아요도 함께 삭제
CREATE TABLE likePoint(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    memberId INT UNSIGNED NOT NULL,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL,
    UNIQUE KEY (memberId, relTypeCode, relId),
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE CASCADE
);

# REPLY 테이블 (댓글) - 회원 탈퇴 시 댓글은 남음 (memberId SET NULL)
CREATE TABLE reply(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    regDate DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    memberId INT UNSIGNED,
    relTypeCode VARCHAR(20) NOT NULL,
    relId INT UNSIGNED NOT NULL,
    content VARCHAR(500) NOT NULL,
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE SET NULL
);

# FILE 테이블 (파일 업로드)
CREATE TABLE `file`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , originName VARCHAR(50) NOT NULL
    , savedName VARCHAR(100) NOT NULL
    , savedPath VARCHAR(100) NOT NULL
);



-- BCrypt 암호화된 '1234' 비밀번호 (너가 쓰던 값 그대로 유지)
SET @BCRYPT_PASSWORD_1234 = '$2a$10$wTfH8y/w5QG7lYh.Q8LzG.8uN/tTj9dYd3uB9tZ2A9D4X7M7O5f9';

INSERT INTO `member`
    SET regDate = NOW()
        , updateDate = NOW()
        , username = 'admin'
        , email = 'admin@example.com'
        , PASSWORD = '$2a$10$ubB6XGHgK6hpobXi3pkck.xfuhjMh1xisU6u8qmYFtPwjn5.pNGki'
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
        , email = 'test1@example.com'
        , PASSWORD = '$2a$10$ubB6XGHgK6hpobXi3pkck.xfuhjMh1xisU6u8qmYFtPwjn5.pNGki'
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
        , email = 'test2@example.com'
        , PASSWORD = '$2a$10$ubB6XGHgK6hpobXi3pkck.xfuhjMh1xisU6u8qmYFtPwjn5.pNGki'
        , realName = '테스트사용자2'
        , nickname = '테스터2'
        , age = 35
        , region = '부산'
        , dailyTarget = 30
        , authLevel = 3;


INSERT INTO board
    SET boardName = '공지사항';

INSERT INTO board
    SET boardName = '질문과 답변';

INSERT INTO article
    SET regDate = NOW()
        , updateDate = NOW()
        , memberId = 2
        , title = '제목1'
        , content = '내용1'
        , boardId = 2;  -- 질문과 답변

INSERT INTO article
    SET regDate = NOW()
        , updateDate = NOW()
        , memberId = 1
        , title = '공지1'
        , content = '공지 내용1'
        , boardId = 1;  -- 공지사항

INSERT INTO article
    SET regDate = NOW()
        , updateDate = NOW()
        , memberId = 3
        , title = '제목3'
        , content = '내용3'
        , boardId = 2;  -- 질문과 답변