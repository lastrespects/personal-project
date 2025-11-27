package com.mmb.service;

import com.mmb.domain.*;
import com.mmb.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // ★ 추가
import org.springframework.http.*; // ★ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URLEncoder; // ★ 추가
import java.nio.charset.StandardCharsets; // ★ 추가
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final MemberRepository memberRepository;
    private final WordRepository wordRepository;
    private final StudyRecordRepository studyRecordRepository;
    
    // API 호출을 위한 도구
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ★ Papago API 키 주입 (application.properties에서 가져옴)
    @Value("${papago.client.id}")
    private String papagoClientId;

    @Value("${papago.client.secret}")
    private String papagoClientSecret;


    // 1. 오늘의 퀴즈 생성
    @Transactional
    public List<StudyRecord> generateDailyQuiz(Long memberId) {
        // ID로 회원을 찾지 못하면 오류 발생 (이전 단계의 NoSuchElementException 방지)
        Member member = memberRepository.findById(memberId).orElseThrow(
            () -> new IllegalArgumentException("회원 ID를 찾을 수 없습니다: " + memberId)
        );
        
        int target = member.getDailyTarget();

        // A. 오늘 복습해야 할 단어 가져오기
        List<StudyRecord> quizList = studyRecordRepository.findTodayReviews(memberId, LocalDate.now());
        
        // B. 목표량이 부족하면 신규 단어 추가 (외부 API 연동)
        if (quizList.size() < target) {
            int needed = target - quizList.size();
            
            // ★ 진짜 API 호출해서 단어 가져오기
            List<Word> newWords = fetchRealWordsFromApi(needed);
            
            for (Word w : newWords) {
                // DB 중복 체크 (이미 있는 단어면 저장 안 함)
                if (!wordRepository.existsBySpelling(w.getSpelling())) {
                    wordRepository.save(w);
                } else {
                    // 이미 있으면 DB에 있는 걸 가져와서 씀 (ID 확보 위해)
                    w = wordRepository.findBySpelling(w.getSpelling()); 
                }
                
                // 사용자의 학습 기록에 추가 (중복 학습 기록 생성 방지)
                if (!studyRecordRepository.existsByMemberAndWord(member, w)) {
                    StudyRecord newRecord = new StudyRecord(member, w);
                    studyRecordRepository.save(newRecord);
                    quizList.add(newRecord);
                }
            }
        }
        
        // 만약 API 호출 실패 등으로 30개가 안 채워졌어도 있는 만큼만 반환
        return quizList;
    }

    // =========================================================
    // ★ [핵심] 외부 API 연동 로직
    // =========================================================
    private List<Word> fetchRealWordsFromApi(int count) {
        List<Word> validWords = new ArrayList<>();
        int tryCount = 0;

        // 원하는 개수가 채워질 때까지 반복 (최대 시도 횟수 제한으로 무한루프 방지)
        while (validWords.size() < count && tryCount < 5) {
            tryCount++;
            try {
                // 1. 랜덤 단어 API 호출 (넉넉하게 요청)
                String randomUrl = "https://random-word-api.herokuapp.com/word?number=" + (count * 2);
                String[] randomWords = restTemplate.getForObject(randomUrl, String[].class);

                if (randomWords == null) continue;

                // 2. 각 단어의 뜻과 음성 조회 (Dictionary API)
                for (String spelling : randomWords) {
                    if (validWords.size() >= count) break;
                    
                    try {
                        String dictUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/" + spelling;
                        String jsonResponse = restTemplate.getForObject(dictUrl, String.class);
                        
                        // JSON 파싱
                        JsonNode root = objectMapper.readTree(jsonResponse);
                        JsonNode firstEntry = root.get(0);
                        
                        // 영어 정의 추출
                        String englishMeaning = firstEntry.path("meanings").get(0).path("definitions").get(0).path("definition").asText();
                        
                        // 예문 추출
                        String example = firstEntry.path("meanings").get(0).path("definitions").get(0).path("example").asText(null);
                        if(example == null || example.isEmpty()) example = "No example available for " + spelling;

                        // 음성 파일 URL 추출
                        String audioUrl = null;
                        JsonNode phonetics = firstEntry.path("phonetics");
                        
                        if (phonetics.isArray()) {
                            for (JsonNode node : phonetics) {
                                if (node.has("audio") && !node.get("audio").asText().isEmpty() && node.get("audio").asText().endsWith(".mp3")) {
                                    audioUrl = node.get("audio").asText();
                                    break; 
                                }
                            }
                        }
                        
                        // ★ 3. 영어 뜻을 한글로 번역
                        String koreanMeaning = translateToKorean(englishMeaning);


                        // 유효한 단어면 리스트에 추가
                        validWords.add(new Word(spelling, koreanMeaning, example, audioUrl)); // ★ koreanMeaning 사용

                    } catch (Exception e) {
                        // 사전 API에 없는 단어는 그냥 건너뜀
                        // System.out.println("사전에 없는 단어 패스: " + spelling);
                    }
                }
            } catch (Exception e) {
                // System.out.println("API 호출 중 오류: " + e.getMessage());
            }
        }
        return validWords;
    }


    // =========================================================
    // ★ [새 함수] Papago API를 사용하여 영어 -> 한글 번역 수행
    // =========================================================
    private String translateToKorean(String englishText) {
        if (englishText == null || englishText.isEmpty()) {
            return "번역할 내용이 없습니다.";
        }

        try {
            String encodedText = URLEncoder.encode(englishText, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://openapi.naver.com/v1/papago/n2mt";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-Naver-Client-Id", papagoClientId);
            headers.set("X-Naver-Client-Secret", papagoClientSecret);

            String body = "source=en&target=ko&text=" + encodedText;
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                request,
                String.class
            );

            // JSON 응답 파싱
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("message").path("result").path("translatedText").asText("번역 실패");

        } catch (Exception e) {
            // System.out.println("번역 API 호출 중 오류: " + e.getMessage());
            return "번역 오류: " + englishText; // 오류 시 원본 영어 뜻 반환
        }
    }


    // 2. 문제 채점 (점수 부여 로직)
    @Transactional
    public String gradeAnswer(Long recordId, String userAnswer) {
        StudyRecord record = studyRecordRepository.findById(recordId).orElseThrow();
        Member member = record.getMember();
        Word word = record.getWord();

        boolean isCorrect = word.getSpelling().equalsIgnoreCase(userAnswer);

        if (isCorrect) {
            // 정답 처리: 망각곡선 적용 (날짜 미루기)
            int step = record.getReviewStep() + 1;
            int days = (int) Math.pow(2, step); // 2, 4, 8일...
            
            record.setReviewStep(step);
            record.setNextReviewDate(LocalDate.now().plusDays(days));
            
            // ★ 포인트 지급 (단어 1개당 10점)
            member.gainExp(10); 
            
            return "정답! 경험치 +10 (다음 복습: " + days + "일 뒤)";
        } else {
            // 오답 처리: 1단계 초기화 & 내일 다시
            record.setReviewStep(0);
            record.setWrongCount(record.getWrongCount() + 1);
            record.setNextReviewDate(LocalDate.now().plusDays(1)); // 내일
            
            return "오답.. 내일 다시 공부하세요.";
        }
    }

    // 3. 힌트 사용 (하루 1회 제한)
    @Transactional
    public String useHint(Long memberId, Long wordId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Word word = wordRepository.findById(wordId).orElseThrow();
        
        // 오늘 날짜 확인
        if (LocalDate.now().equals(member.getLastHintDate())) {
            return "실패: 오늘은 이미 힌트를 사용했습니다.";
        }

        // 힌트 제공 (첫 글자 보여주기 or 중간 글자)
        member.setLastHintDate(LocalDate.now()); // 사용 기록 저장
        return "힌트: 첫 글자는 [" + word.getSpelling().charAt(0) + "] 입니다.";
    }
}