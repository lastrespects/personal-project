package com.mmb.service;

import com.mmb.dao.LikePointDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikePointWriteService {

    private final LikePointDao likePointDao;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void insertLikePoint(int memberId, String relTypeCode, int relId) {
        logWriteState("LIKE_INSERT", memberId, relTypeCode, relId);
        likePointDao.insertLikePoint(memberId, relTypeCode, relId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteLikePoint(int memberId, String relTypeCode, int relId) {
        logWriteState("LIKE_DELETE", memberId, relTypeCode, relId);
        likePointDao.deleteLikePoint(memberId, relTypeCode, relId);
    }

    private void logWriteState(String action, int memberId, String relTypeCode, int relId) {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug("[WRITE_TRY] action={} memberId={} relTypeCode={} relId={} txActive={} txReadOnly={}",
                action, memberId, relTypeCode, relId, txActive, txReadOnly);
    }
}
