-- src/main/resources/data.sql

-- 1. 테이블이 존재하면 삭제 (재시작 시 오류 방지)
DROP TABLE IF EXISTS WORD;
DROP TABLE IF EXISTS MEMBER;
DROP TABLE IF EXISTS STUDY_RECORD;

-- 2. WORD 테이블 생성 (Word.java를 참고하여 필드를 맞춰야 합니다.)
CREATE TABLE WORD (
    id BIGINT NOT NULL,
    spelling VARCHAR(255) NOT NULL,
    meaning VARCHAR(255) NOT NULL,
    example_sentence VARCHAR(500),
    audio_path VARCHAR(255),
    PRIMARY KEY (id)
);

-- 3. MEMBER 테이블 생성 (Member.java를 참고)
CREATE TABLE MEMBER (
    id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    daily_target INT NOT NULL,
    character_level INT NOT NULL,
    current_exp INT NOT NULL,
    last_hint_date DATE,
    PRIMARY KEY (id)
);

-- 4. STUDY_RECORD 테이블 생성 (StudyRecord.java를 참고)
CREATE TABLE STUDY_RECORD (
    id BIGINT NOT NULL,
    member_id BIGINT,
    word_id BIGINT,
    review_date DATE,
    next_review_date DATE,
    wrong_count INT,
    PRIMARY KEY (id),
    FOREIGN KEY (member_id) REFERENCES MEMBER(id),
    FOREIGN KEY (word_id) REFERENCES WORD(id)
);

-- 5. 실제 단어 데이터 삽입
-- 테이블이 만들어진 후에 삽입 쿼리가 실행되어야 합니다.
INSERT INTO WORD (id, spelling, meaning, example_sentence, audio_path) 
VALUES (1, 'apple', '사과', 'I ate an apple for breakfast.', 'apple.mp3');

INSERT INTO WORD (id, spelling, meaning, example_sentence, audio_path) 
VALUES (2, 'banana', '바나나', 'I like to eat a banana with milk.', 'banana.mp3');