DROP TABLE IF EXISTS STUDY_RECORD;
DROP TABLE IF EXISTS WORD;
DROP TABLE IF EXISTS MEMBER;

CREATE TABLE WORD (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spelling VARCHAR(255) NOT NULL,
    meaning VARCHAR(1000),
    example_sentence VARCHAR(1000),
    audio_path VARCHAR(255)
);

CREATE TABLE MEMBER (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(255),
    daily_target INT,
    character_level INT,
    current_exp INT,
    last_hint_date DATE
);

CREATE TABLE STUDY_RECORD (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    word_id BIGINT,
    next_review_date DATE,
    wrong_count INT,
    FOREIGN KEY (member_id) REFERENCES MEMBER(id),
    FOREIGN KEY (word_id) REFERENCES WORD(id)
);

INSERT INTO WORD (spelling, meaning, example_sentence, audio_path) VALUES ('apple', '사과', 'I ate an apple for breakfast.', 'apple.mp3');
INSERT INTO WORD (spelling, meaning, example_sentence, audio_path) VALUES ('banana', '바나나', 'I like to eat a banana with milk.', 'banana.mp3');
