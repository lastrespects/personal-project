package com.mmb.service;

import com.mmb.dao.ReplyDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyWriteService {

    private final ReplyDao replyDao;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public int writeReply(int memberId, String relTypeCode, int relId, String content) {
        logWriteState("REPLY_INSERT", memberId, relTypeCode, relId);
        replyDao.writeReply(memberId, relTypeCode, relId, content);
        return replyDao.getLastInsertId();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void deleteReply(int id) {
        logWriteState("REPLY_DELETE", id, null, id);
        replyDao.deleteReply(id);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void modifyReply(int id, String content) {
        logWriteState("REPLY_UPDATE", id, null, id);
        replyDao.modifyReply(id, content);
    }

    private void logWriteState(String action, Integer memberId, String relTypeCode, Integer relId) {
        boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean txReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug("[WRITE_TRY] action={} memberId={} relTypeCode={} relId={} txActive={} txReadOnly={}",
                action, memberId, relTypeCode, relId, txActive, txReadOnly);
    }
}
