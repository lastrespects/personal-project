DROP DATABASE IF EXISTS SB_AM;
CREATE DATABASE SB_AM;
USE SB_AM;

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

CREATE TABLE `member`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , loginId VARCHAR(50) NOT NULL
    , loginPw VARCHAR(100) NOT NULL
    , `name` VARCHAR(20) NOT NULL
    , authLevel INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '관리자 = 0, 사용자 = 1'
    
    -- [학습 기능 필드 추가]
    , dailyTarget INT UNSIGNED NOT NULL DEFAULT 10
    , characterLevel INT UNSIGNED NOT NULL DEFAULT 1
    , currentExp INT UNSIGNED NOT NULL DEFAULT 0
);

CREATE TABLE board(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , boardName VARCHAR(20) NOT NULL
);

CREATE TABLE likePoint(
    memberId INT NOT NULL
    , relTypeCode VARCHAR(20) NOT NULL
    , relId INT UNSIGNED NOT NULL
);

CREATE TABLE reply(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , updateDate DATETIME NOT NULL
    , memberId INT UNSIGNED NOT NULL
    , relTypeCode VARCHAR(20) NOT NULL
    , relId INT UNSIGNED NOT NULL
    , content VARCHAR(500) NOT NULL
);

CREATE TABLE `file`(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT
    , regDate DATETIME NOT NULL
    , originName VARCHAR(50) NOT NULL
    , savedName VARCHAR(100) NOT NULL
    , savedPath VARCHAR(100) NOT NULL
);

-- [JPA 엔티티용 테이블]
CREATE TABLE word(
    id BIGINT PRIMARY KEY AUTO_INCREMENT
    , spelling VARCHAR(50) NOT NULL UNIQUE
    , meaning VARCHAR(500)
    , example_sentence VARCHAR(1000)
    , audio_path VARCHAR(255)
);

CREATE TABLE study_record(
    id BIGINT PRIMARY KEY AUTO_INCREMENT
    , member_id INT UNSIGNED NOT NULL
    , word_id BIGINT NOT NULL
    , review_step INT NOT NULL
    , wrong_count INT NOT NULL DEFAULT 0
    , next_review_date DATE NOT NULL
    , FOREIGN KEY (member_id) REFERENCES `member`(id)
    , FOREIGN KEY (word_id) REFERENCES word(id)
);


-- [더미 데이터는 사용자님의 기존 코드를 유지하며, member 테이블에 학습 필드 기본값 추가]

INSERT INTO article
    SET regDate = NOW()
        , updateDate = NOW()
        , memberId = 2
        , title = '제목1'
        , content = '내용1'
        , boardId = 2;
        
-- ... (나머지 기존 더미 데이터 유지) ...