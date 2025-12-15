<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <title>${boardName} - My Memory Book</title>

  <style>
    /* 이 페이지 전용 약간만 */
    .page-head{
      display:flex;
      justify-content: space-between;
      align-items:flex-end;
      gap: 12px;
      flex-wrap: wrap;
      padding: 20px;
    }
    .page-head .left{
      display:flex;
      flex-direction: column;
      gap: 8px;
    }
    .meta-line{
      color: var(--muted);
      font-weight: 650;
      display:flex;
      gap: 10px;
      align-items:center;
      flex-wrap: wrap;
    }

    .search-card{ padding: 16px; margin-top: 14px; }
    .search-row{
      display:flex;
      gap:10px;
      align-items:center;
      flex-wrap:wrap;
    }
    .search-row select{ width: 160px; }
    .search-row input[type="text"]{ width: 220px; }

    .table-card{ margin-top: 14px; overflow:hidden; }
    .table-card table td a:hover{
      text-decoration: underline;
      text-underline-offset: 4px;
    }

    .bottom-bar{
      margin-top: 14px;
      display:flex;
      justify-content: space-between;
      align-items:center;
      gap: 10px;
      flex-wrap: wrap;
    }

    .pagination{
      margin-top: 14px;
      display:flex;
      justify-content:center;
      gap: 6px;
      flex-wrap: wrap;
    }
    .pagination a{
      display:inline-flex;
      align-items:center;
      justify-content:center;
      min-width: 40px;
      height: 40px;
      padding: 0 12px;
      border-radius: 12px;
      border: 1px solid var(--line);
      background:#fff;
      font-weight: 800;
      color: var(--muted);
    }
    .pagination a:hover{
      border-color:#c7d2fe;
      box-shadow: 0 10px 22px rgba(15,23,42,.08);
      color: var(--text);
    }
    .pagination a.active{
      border-color: transparent;
      background: var(--primary);
      color:#fff;
    }
  </style>
</head>

<body>
<%@ include file="/view/usr/common/header.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- 상단 타이틀 카드 -->
<section class="card page-head">
  <div class="left">
    <div>
      <span class="badge primary">${boardName}</span>
    </div>
    <h1 class="title" style="margin:0;">${boardName} 게시판</h1>
    <div class="meta-line">
      <span>총 <b style="color:var(--text);">${articlesCnt}</b>건</span>
      <c:if test="${param.boardId == '1'}">
        <span class="badge">공지</span>
      </c:if>
      <c:if test="${param.boardId == '2'}">
        <span class="badge">Q&amp;A</span>
      </c:if>
    </div>
  </div>

  <!-- 우측 버튼 영역 -->
  <div class="right" style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
    <a class="btn" href="${ctx}/usr/home/main">메인</a>
    <c:if test="${isLoggedIn and (param.boardId ne '1' or isAdmin)}">
      <a class="btn btn-primary" href="${ctx}/usr/article/write?boardId=${param.boardId}">글쓰기</a>
    </c:if>
  </div>
</section>

<!-- 검색 카드 -->
<section class="card search-card">
  <form method="get" action="${ctx}/usr/article/list" class="search-row">
    <input type="hidden" name="boardId" value="${param.boardId}" />

    <select name="searchType">
      <option value="title" <c:if test="${param.searchType == 'title'}">selected</c:if>>제목</option>
      <option value="content" <c:if test="${param.searchType == 'content'}">selected</c:if>>내용</option>
      <option value="title,content" <c:if test="${param.searchType == 'title,content'}">selected</c:if>>제목+내용</option>
    </select>

    <input type="text" name="searchKeyword" value="${param.searchKeyword}" placeholder="검색어" />

    <button type="submit" class="btn btn-primary">검색</button>
  </form>
</section>

<!-- 목록 테이블 카드 -->
<section class="card table-card">
  <table>
    <tr>
      <th style="width:90px;">번호</th>
      <th>제목</th>
      <th style="width:140px;">작성자</th>
      <th style="width:190px;">작성일</th>
      <th style="width:90px;">좋아요</th>
      <th style="width:90px;">조회</th>
    </tr>

    <c:forEach items="${articles}" var="article">
      <tr>
        <td>${article.id}</td>
        <td>
          <a href="${ctx}/usr/article/detail?id=${article.id}">
            ${article.title}
          </a>
        </td>
        <td>${article.writerName}</td>
        <td>${article.regDate}</td>
        <td>${article.likeCount}</td>
        <td>${article.views}</td>
      </tr>
    </c:forEach>
  </table>
</section>

<!-- 하단 버튼/페이지네이션 -->
<div class="bottom-bar">
  <a class="btn" href="${ctx}/usr/home/main">메인으로</a>
</div>

<div class="pagination">
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

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
