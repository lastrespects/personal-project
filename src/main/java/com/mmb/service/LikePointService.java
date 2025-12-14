// LikePointService.java
package com.mmb.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmb.dao.LikePointDao; // 패키지 변경
import com.mmb.dto.LikePoint; // 패키지 변경

@Service
@Transactional(readOnly = true)
public class LikePointService {

	private LikePointDao likePointDao;
	
	public LikePointService(LikePointDao likePointDao) {
		this.likePointDao = likePointDao;
	}

	public int getLikePointCnt(String relTypeCode, int relId) {
		return this.likePointDao.getLikePointCnt(relTypeCode, relId);
	}

	public LikePoint getLikePoint(int memberId, String relTypeCode, int relId) {
		return this.likePointDao.getLikePoint(memberId, relTypeCode, relId);
	}
	
}
