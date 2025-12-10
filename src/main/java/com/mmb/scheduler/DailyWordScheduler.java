package com.mmb.scheduler;

import com.mmb.service.WordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyWordScheduler {

    private final WordService wordService;
    private final int dailyWordCount;

    public DailyWordScheduler(WordService wordService,
                              @Value("${daily.word.count}") int dailyWordCount) {
        this.wordService = wordService;
        this.dailyWordCount = dailyWordCount;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void fetchDailyNewWords() {
        System.out.println("--- [스케줄러] 매일 새로운 단어 가져오기 시작 (개수: " + dailyWordCount + ") ---");
        try {
            wordService.fetchAndSaveMockWords(dailyWordCount);
            System.out.println("--- [스케줄러] 단어 " + dailyWordCount + "개 저장 완료 ---");
        } catch (Exception e) {
            System.err.println("--- [스케줄러] 단어 저장 중 오류 발생: " + e.getMessage() + " ---");
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void initialLoadCheck() {
        if (wordService.findAllWords().isEmpty()) {
            System.out.println("--- [초기 로드] DB에 단어가 없어 초기 " + dailyWordCount + "개 단어 생성 시작 ---");
            wordService.fetchAndSaveMockWords(dailyWordCount);
            System.out.println("--- [초기 로드] 초기 단어 " + dailyWordCount + "개 생성 완료 ---");
        }
    }
}
