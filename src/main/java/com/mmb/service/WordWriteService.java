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
    private final org.springframework.transaction.PlatformTransactionManager transactionManager;

    public Word saveNewWord(Word word) {
        if (word == null || word.getSpelling() == null || word.getSpelling().isBlank()) {
            throw new IllegalArgumentException("word spelling is required");
        }

        // ✅ spelling 정규화
        String spelling = normalizeSpelling(word.getSpelling());
        word.setSpelling(spelling);

        logWriteState("WORD_INSERT_TRY", spelling);

        org.springframework.transaction.support.TransactionTemplate template = new org.springframework.transaction.support.TransactionTemplate(
                transactionManager);
        template.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return template.execute(status -> {
            try {
                Word saved = wordRepository.save(word);
                if (saved.getId() == null) {
                    throw new IllegalStateException("Word id is null after save");
                }
                return saved;
            } catch (DataIntegrityViolationException e) {
                // ✅ uq_word_spelling 충돌 -> status.setRollbackOnly() implicitly handled by
                // exception?
                // No, we catch it inside the template. The template tx will be rolled back?
                // RequiresNew tx will be rolled back.
                // But we want to Recover.
                // If we catch it here, the TX is marked rollback only?
                // Yes, DataIntegrityViolation marks it rollbackOnly.
                // But this is the INNER tx. It rolling back is FINE.
                // We return existing word.

                log.warn("[WORD_INSERT_DUP] spelling={} msg={}", spelling,
                        e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
                return wordRepository.findFirstBySpellingIgnoreCase(spelling)
                        .orElseThrow(() -> e); // 정말 없으면 throw
            } catch (Exception e) {
                // Other exceptions
                log.error("[WORD_INSERT_FAIL] spelling={}", spelling, e);
                throw e;
            }
        });
    }

    private String normalizeSpelling(String s) {
        if (s == null)
            return "";
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
