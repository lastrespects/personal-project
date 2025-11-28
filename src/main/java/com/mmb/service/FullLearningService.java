package com.mmb.service;

import com.mmb.domain.Member;
import com.mmb.domain.StudyRecord;
import com.mmb.domain.Word;
import com.mmb.dto.MemberCreateDto;
import com.mmb.dto.StudyRecordDto;
import com.mmb.dto.WordDto;
import com.mmb.repository.MemberRepository;
import com.mmb.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 학습 전체 로직 - 최적화 버전 (비동기 처리)
 */
@Service
@RequiredArgsConstructor
public class FullLearningService {

    private final MemberRepository memberRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final WordService wordService;

    /**
     * 회원 가입
     */
    @Transactional
    public Member join(MemberCreateDto dto) {
        if (memberRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
        Member m = Member.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .dailyTarget(dto.getDailyTarget())
                .build();
        return memberRepository.save(m);
    }

    /**
     * 일일 퀴즈 생성 (비동기 처리)
     */
    @Transactional(readOnly = true)
    public List<StudyRecordDto> generateDailyQuizDto(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        List<StudyRecord> today = studyRecordRepository.findTodayReviews(memberId, LocalDate.now());
        int remaining = Math.max(0, member.getDailyTarget() - today.size());

        if (remaining > 0) {
            List<String> randomWords = fetchRandomWords(Math.max(remaining * 2, remaining + 5));

            // 비동기 Word 처리
            List<CompletableFuture<Optional<Word>>> futures = new ArrayList<>();
            for (String sp : randomWords) {
                futures.add(findOrCreateAsync(sp));
            }

            for (CompletableFuture<Optional<Word>> f : futures) {
                try {
                    Optional<Word> opt = f.get(); // 결과 대기
                    if (opt.isPresent()) {
                        Word w = opt.get();
                        if (!studyRecordRepository.existsByMemberAndWord(member, w) && today.size() < member.getDailyTarget()) {
                            StudyRecord rec = StudyRecord.builder()
                                    .member(member)
                                    .word(w)
                                    .reviewStep(0)
                                    .wrongCount(0)
                                    .nextReviewDate(LocalDate.now())
                                    .build();
                            studyRecordRepository.save(rec);
                            today.add(rec);
                        }
                        if (today.size() >= member.getDailyTarget()) break;
                    }
                } catch (Exception e) {
                    System.err.println("비동기 단어 처리 실패: " + e.getMessage());
                }
            }
        }

        List<StudyRecordDto> result = new ArrayList<>();
        for (StudyRecord r : today) {
            Word w = r.getWord();
            WordDto wd = new WordDto(w.getSpelling(), w.getMeaning(), w.getExampleSentence(), w.getAudioPath());
            result.add(new StudyRecordDto(r.getId(), wd, r.getReviewStep(), r.getWrongCount(), r.getNextReviewDate()));
        }

        return result;
    }

    /**
     * 랜덤 단어 API 호출
     */
    private List<String> fetchRandomWords(int number) {
        try {
            java.net.URL url = new java.net.URL("https://random-word-api.herokuapp.com/word?number=" + number);
            try (java.io.InputStream in = url.openStream()) {
                String json = new String(in.readAllBytes());
                json = json.replaceAll("[\\[\\]\"]", "");
                if (json.isBlank()) return List.of();
                return Arrays.asList(json.split(","));
            }
        } catch (Exception e) {
            System.err.println("random word fetch failed: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 비동기 단어 생성/조회
     * - @Transactional 적용으로 트랜잭션 안전
     */
    @Async
    @Transactional
    public CompletableFuture<Optional<Word>> findOrCreateAsync(String spelling) {
        try {
            Word w = wordService.findOrCreate(spelling);
            return CompletableFuture.completedFuture(Optional.of(w));
        } catch (Exception e) {
            System.err.println("findOrCreateAsync 실패: " + e.getMessage());
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * 틀림 처리
     */
    @Transactional
    public void markWrong(Long studyRecordId) {
        StudyRecord rec = studyRecordRepository.findById(studyRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid study record id"));

        int currentWrong = rec.getWrongCount();
        rec.setWrongCount(currentWrong + 1);

        LocalDate next;
        if (currentWrong == 0) {
            next = LocalDate.now().plusDays(7); // 첫 틀림 → 7일 후
        } else {
            next = LocalDate.now().plusDays(3); // 두 번째 이상 틀림 → 3일 후
        }
        rec.setNextReviewDate(next);
        studyRecordRepository.save(rec);
    }
}
