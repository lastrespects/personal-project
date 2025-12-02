package com.mmb.dao;

import com.mmb.dto.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleDao {

    // 게시글 작성
    @Insert("""
            INSERT INTO article
            SET regDate = NOW(),
                updateDate = NOW(),
                memberId = #{memberId},
                title = #{title},
                content = #{content},
                boardId = #{boardId},
                views = 0
            """)
    void writeArticle(@Param("title") String title,
                      @Param("content") String content,
                      @Param("memberId") int memberId,
                      @Param("boardId") int boardId);

    // 마지막 AUTO_INCREMENT id
    @Select("SELECT LAST_INSERT_ID()")
    int getLastInsertId();

    // 게시글 개수 (일단 boardId 기준만, 검색은 나중에)
    @Select("""
            SELECT COUNT(*)
            FROM article a
            WHERE a.boardId = #{boardId}
            """)
    int getArticlesCnt(@Param("boardId") int boardId,
                       @Param("searchType") String searchType,
                       @Param("searchKeyword") String searchKeyword);

    // 게시글 목록 (boardId + 페이징만, 검색은 나중에)
    @Select("""
            SELECT
                a.id,
                a.regDate,
                a.title,
                m.username AS writerName,
                IFNULL(COUNT(l.memberId), 0) AS likePoint,
                a.views
            FROM article a
            JOIN member m
              ON a.memberId = m.id
            LEFT JOIN likePoint l
              ON l.relTypeCode = 'article'
             AND l.relId = a.id
            WHERE a.boardId = #{boardId}
            GROUP BY
                a.id,
                a.regDate,
                a.title,
                m.username,
                a.views
            ORDER BY a.id DESC
            LIMIT #{limitFrom}, #{itemsInAPage}
            """)
    List<Article> showList(@Param("boardId") int boardId,
                           @Param("limitFrom") int limitFrom,
                           @Param("itemsInAPage") int itemsInAPage,
                           @Param("searchType") String searchType,
                           @Param("searchKeyword") String searchKeyword);

    // 조회수 증가
    @Update("""
            UPDATE article
            SET views = views + 1,
                updateDate = NOW()
            WHERE id = #{id}
            """)
    void increaseViews(@Param("id") int id);

    // 게시글 상세
    @Select("""
            SELECT
                a.id,
                a.regDate,
                a.updateDate,
                a.memberId,
                a.title,
                a.content,
                a.boardId,
                a.views,
                m.username AS writerName,
                IFNULL(COUNT(l.memberId), 0) AS likePoint
            FROM article a
            JOIN member m
              ON a.memberId = m.id
            LEFT JOIN likePoint l
              ON l.relTypeCode = 'article'
             AND l.relId = a.id
            WHERE a.id = #{id}
            GROUP BY
                a.id,
                a.regDate,
                a.updateDate,
                a.memberId,
                a.title,
                a.content,
                a.boardId,
                a.views,
                m.username
            """)
    Article getArticleById(@Param("id") int id);

    // 게시글 수정
    @Update("""
            UPDATE article
            SET
                updateDate = NOW(),
                title = #{title},
                content = #{content}
            WHERE id = #{id}
            """)
    void modifyArticle(@Param("id") int id,
                       @Param("title") String title,
                       @Param("content") String content);

    // 게시글 삭제
    @Delete("""
            DELETE FROM article
            WHERE id = #{id}
            """)
    void deleteArticle(@Param("id") int id);
}
