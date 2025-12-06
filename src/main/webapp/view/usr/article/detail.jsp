<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${article.title}</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { margin-bottom: 6px; }
        .meta { color: #777; margin-bottom: 12px; }
        table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; vertical-align: top; }
        th { background: #f5f5f5; width: 120px; }
        .actions { margin-top: 14px; display: flex; gap: 8px; }
        .actions a { padding: 6px 10px; border: 1px solid #333; text-decoration: none; font-size: 13px; background: #fff; }
        .content { line-height: 1.6; white-space: pre-wrap; }
    </style>
</head>
<body>
    <c:set var="canEdit" value="${canEdit}" />

    <h1>${article.title}</h1>
    <div class="meta">작성자: ${article.writerName} | 작성일: ${article.regDate} | 조회: ${article.views} | 추천: ${article.likePoint}</div>

    <table>
        <tr>
            <th>작성일</th>
            <td>${article.regDate}</td>
        </tr>
        <tr>
            <th>수정일</th>
            <td>${article.updateDate}</td>
        </tr>
        <tr>
            <th>작성자</th>
            <td>${article.writerName}</td>
        </tr>
        <tr>
            <th>내용</th>
            <td class="content">${article.content}</td>
        </tr>
    </table>

    <div class="actions">
        <a href="/usr/article/list?boardId=${article.boardId}">목록으로</a>
        <c:if test="${canEdit}">
            <a href="/usr/article/modify?id=${article.id}">수정</a>
            <a href="/usr/article/delete?id=${article.id}&boardId=${article.boardId}" onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
        </c:if>
    </div>
</body>
</html>
