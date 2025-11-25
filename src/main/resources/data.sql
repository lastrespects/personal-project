-- src/main/resources/data.sql

-- 1. WORD 테이블에 단어 삽입
INSERT INTO WORD (id, spelling, meaning, example_sentence, audio_path) 
VALUES (1, 'apple', '사과', 'I ate an apple for breakfast.', 'apple.mp3');

INSERT INTO WORD (id, spelling, meaning, example_sentence, audio_path) 
VALUES (2, 'banana', '바나나', 'I like to eat a banana with milk.', 'banana.mp3');