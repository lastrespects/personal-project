// ReplyService.java
package com.mmb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmb.dao.ReplyDao; // 패키지 변경
import com.mmb.dto.Reply; // 패키지 변경

@Service
@Transactional(readOnly = true)
public class ReplyService {

	private ReplyDao replyDao;
	
	public ReplyService(ReplyDao replyDao) {
		this.replyDao = replyDao;
	}
	
	public List<Reply> getReplies(String relTypeCode, int relId) {
		return this.replyDao.getReplies(relTypeCode, relId);
	}

	public Reply getReply(int id) {
		return this.replyDao.getReply(id);
	}
}
