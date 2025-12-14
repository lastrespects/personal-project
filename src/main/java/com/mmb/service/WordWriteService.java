package com.mmb.service;

import com.mmb.entity.Word;
import com.mmb.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordWriteService {

    private final WordRepository wordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Word saveNewWord(Word word) {
        if (word == null || word.getSpelling() == null || word.getSpelling().isBlank()) {
            throw new IllegalArgumentException("word spelling is required");
        }

        // ✅ spelling 정규화 (DB UNIQUE 기준과 최대한 일치)
        String spelling = normalizeSpelling(word.getSpelling());
        word.setSpelling(spelling);

        logWriteState("WORD_INSERT_TRY", spelling);

        try {
            Word saved = wordRepository.save(word);
            if (saved.getId() == null) {
                throw new IllegalStateException("Word id is null after save");
            }
            return saved;
        } catch (DataIntegrityViolationException e) {
            // ✅ uq_word_spelling 충돌이면 "기존 단어"를 재사용
            log.warn("[WORD_INSERT_DUP] spelling={} msg={}", spelling, e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());

            return wordRepository.findFirstBySpellingIgnoreCase(spelling)
                    .orElseThrow(() -> e); // 정말 없으면 그대로 throw
        }
    }

    private String normalizeSpelling(String s) {
        if (s == null) return "";
        // 공백/줄바꿈 제거 + 양끝 trim + 소문자
        return s.replace("\r", " ")
                .replace("\n", " ")
                .trim()
                .toLowerCase();
    }

    private void logWriteState(String action, Object detail) {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug("[WRITE_TRY] action={} detail={} txActive={} txReadOnly={}",
                action, detail, txActive, txReadOnly);
    }
}
