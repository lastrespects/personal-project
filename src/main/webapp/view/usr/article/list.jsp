<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${boardName} 게시판</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { margin-bottom: 6px; }
        .meta { color: #777; margin-bottom: 16px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background: #f5f5f5; }
        tr:hover { background: #fafafa; }
        .actions { margin: 12px 0; display: flex; justify-content: space-between; align-items: center; }
        .actions a { padding: 6px 10px; border: 1px solid #333; background: #fff; text-decoration: none; font-size: 13px; }
        .pagination a { margin: 0 3px; padding: 6px 10px; border: 1px solid #ccc; text-decoration: none; color: #333; }
        .pagination .active { background: #333; color: #fff; border-color: #333; }
        form.search { display: flex; gap: 8px; align-items: center; margin-bottom: 14px; }
        form.search input[type="text"] { padding: 6px 8px; width: 200px; }
        form.search select { padding: 6px 8px; }
    </style>
</head>
<body>
    <h1>${boardName} 게시판</h1>
    <div class="meta">총 ${articlesCnt}개</div>

    <form class="search" method="get" action="/usr/article/list">
        <input type="hidden" name="boardId" value="${param.boardId}" />
        <select name="searchType">
            <option value="title" <c:if test="${param.searchType == 'title'}">selected</c:if>>제목</option>
            <option value="content" <c:if test="${param.searchType == 'content'}">selected</c:if>>내용</option>
            <option value="title,content" <c:if test="${param.searchType == 'title,content'}">selected</c:if>>제목+내용</option>
        </select>
        <input type="text" name="searchKeyword" value="${param.searchKeyword}" placeholder="검색어" />
        <button type="submit">검색</button>
    </form>

    <table>
        <tr>
            <th>번호</th>
            <th>제목</th>
            <th>작성자</th>
            <th>작성일</th>
            <th>추천</th>
            <th>조회</th>
        </tr>
        <c:forEach items="${articles}" var="article">
            <tr>
                <td>${article.id}</td>
                <td><a href="/usr/article/detail?id=${article.id}">${article.title}</a></td>
                <td>${article.writerName}</td>
                <td>${article.regDate}</td>
                <td>${article.likePoint}</td>
                <td>${article.views}</td>
            </tr>
        </c:forEach>
    </table>

    <div class="actions">
        <a href="/usr/home/main">메인으로</a>
        <c:if test="${isLoggedIn and (param.boardId ne '1' or isAdmin)}">
            <a href="/usr/article/write?boardId=${param.boardId}">글쓰기</a>
        </c:if>
    </div>

    <div class="pagination" style="text-align:center; margin-top:12px;">
        <c:set var="queryString" value="?boardId=${param.boardId}&searchType=${param.searchType}&searchKeyword=${param.searchKeyword}" />
        <c:if test="${begin != 1}">
            <a href="${queryString}&cPage=1">«</a>
            <a href="${queryString}&cPage=${begin - 1}">‹</a>
        </c:if>
        <c:forEach begin="${begin}" end="${end}" var="i">
            <a class="${cPage == i ? 'active' : ''}" href="${queryString}&cPage=${i}">${i}</a>
        </c:forEach>
        <c:if test="${end != totalPagesCnt}">
            <a href="${queryString}&cPage=${end + 1}">›</a>
            <a href="${queryString}&cPage=${totalPagesCnt}">»</a>
        </c:if>
    </div>
</body>
</html>
