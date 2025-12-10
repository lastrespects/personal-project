// src/main/java/com/mmb/service/ArticleService.java
package com.mmb.service;

import com.mmb.dao.ArticleDao;
import com.mmb.dto.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleDao articleDao;

    // 게시글 작성
    public void writeArticle(String title, String content, int memberId, int boardId) {
        articleDao.writeArticle(title, content, memberId, boardId);
    }

    // 마지막 insert id
    public int getLastInsertId() {
        return articleDao.getLastInsertId();
    }

    // 게시글 수
    public int getArticlesCnt(int boardId, String searchType, String searchKeyword) {
        return articleDao.getArticlesCnt(boardId, searchType, searchKeyword);
    }

    // 게시글 리스트 (페이징)
    public List<Article> showList(int boardId,
                                  int limitFrom,
                                  int itemsInAPage,
                                  String searchType,
                                  String searchKeyword) {

        return articleDao.showList(boardId, limitFrom, itemsInAPage, searchType, searchKeyword);
    }

    // 조회수 증가
    public void increaseViews(int id) {
        articleDao.increaseViews(id);
    }

    // 게시글 하나
    public Article getArticleById(int id) {
        return articleDao.getArticleById(id);
    }

    // 수정
    public void modifyArticle(int id, String title, String content) {
        articleDao.modifyArticle(id, title, content);
    }

    // 삭제
    public void deleteArticle(int id) {
        articleDao.deleteArticle(id);
    }

    // 🔹 메인화면 공지용: 특정 게시판(boardId)의 최신 글 N개
    public List<Article> findLatestArticles(int boardId, int limit) {
        // boardId 기준, 검색 없이 최신 글 limit개
        return articleDao.showList(boardId, 0, limit, "", "");
    }
}
