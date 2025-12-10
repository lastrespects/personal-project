// BoardService.java
package com.mmb.service;

import org.springframework.stereotype.Service;

import com.mmb.dao.BoardDao; // 패키지 변경

@Service
public class BoardService {

	private BoardDao boardDao;
	
	public BoardService(BoardDao boardDao) {
		this.boardDao = boardDao;
	}

	public String getBoardNameById(int boardId) {
		return this.boardDao.getBoardNameById(boardId);
	}
}