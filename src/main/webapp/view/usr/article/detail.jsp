<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <title>${article.title} - My Memory Book</title>

  <style>
    /* ====== 이 페이지 전용 보정 ====== */
    .article-head {
      padding: 22px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .article-meta {
      color: var(--muted);
      font-weight: 650;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
    }

    /* 본문(내용) 칸 크게 */
    .content-cell { padding: 22px; }
    .content-text{
      display:block;
      min-height: 260px;    /* ✅ 여기 숫자 올리면 더 커짐 */
      line-height: 1.75;
      white-space: pre-wrap;
      font-weight: 650;
    }

    /* actions */
    .actions {
      display:flex;
      gap:10px;
      flex-wrap:wrap;
      align-items:center;
      margin-top: 14px;
    }
    .actions a { display:inline-flex; align-items:center; justify-content:center; }

    /* 좋아요 */
    .like-section{
      display:flex;
      gap:10px;
      align-items:center;
      flex-wrap:wrap;
      padding: 18px;
    }
    .like-btn{
      border:1px solid var(--line);
      background:#fff;
      padding: 10px 14px;
      border-radius: 12px;
      cursor:pointer;
      font-weight: 800;
      letter-spacing: -.2px;
      transition: transform .05s ease, box-shadow .2s ease, border-color .2s ease, background .2s ease;
    }
    .like-btn:hover{
      box-shadow: 0 10px 22px rgba(15,23,42,.08);
      border-color:#c7d2fe;
    }
    .like-btn:active{ transform: translateY(1px); }
    .like-btn.liked{
  border-color: transparent;
  background: #eef2ff;
  color: #3730a3;
  font-weight: 900;
}

    /* 댓글 */
    .reply-section{ margin-top: 18px; }
    .reply-head{
      display:flex;
      justify-content: space-between;
      align-items:center;
      gap:10px;
      padding: 18px 18px 0;
    }
    .reply-list{ padding: 18px; display:flex; flex-direction: column; gap: 12px; }
    .reply-item{
      border: 1px solid var(--line);
      border-radius: 14px;
      background:#fff;
      padding: 14px;
    }
    .reply-meta{
      font-size: 13px;
      color: var(--muted);
      font-weight: 700;
      display:flex;
      justify-content: space-between;
      gap:10px;
      margin-bottom: 8px;
    }
    .reply-body{
      white-space: pre-wrap;
      font-weight: 650;
      line-height: 1.6;
      margin: 8px 0 12px;
    }
    .reply-actions{
      display:flex;
      gap:10px;
      align-items:center;
      flex-wrap:wrap;
    }
    .reply-like-btn{
      border:1px solid var(--line);
      background:#fff;
      padding: 8px 12px;
      border-radius: 12px;
      cursor:pointer;
      font-weight: 800;
      display:inline-flex;
      gap:8px;
      align-items:center;
    }
    .reply-like-btn.liked{
      border-color: transparent;
      background: #eef2ff;
      color: #3730a3;
    }
    .reply-delete-btn{
      border:1px solid #fecaca;
      background:#fff1f2;
      color:#b91c1c;
      padding: 8px 12px;
      border-radius: 12px;
      cursor:pointer;
      font-weight: 900;
    }

    .reply-form{
      padding: 0 18px 18px;
      display:flex;
      flex-direction: column;
      gap:10px;
    }
    .reply-form textarea{
      min-height: 130px;
    }
    .muted{
      color: var(--muted);
      font-weight: 650;
      padding: 0 18px 18px;
      margin: 0;
    }
  </style>
</head>

<body data-article-id="${article.id}">
<%@ include file="/view/usr/common/header.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="canEdit" value="${canEdit}" />

<!-- ✅ 게시판명 라벨 만들기 (boardName 있으면 우선 사용 / 없으면 boardId로 fallback) -->
<c:set var="boardLabel" value="" />
<c:choose>
  <c:when test="${not empty boardName}">
    <c:set var="boardLabel" value="${boardName}" />
  </c:when>
  <c:when test="${article.boardId == 1}">
    <c:set var="boardLabel" value="공지사항" />
  </c:when>
  <c:when test="${article.boardId == 2}">
    <c:set var="boardLabel" value="Q&amp;A" />
  </c:when>
  <c:otherwise>
    <c:set var="boardLabel" value="게시판" />
  </c:otherwise>
</c:choose>

<!-- ====== 상단 헤더 카드 ====== -->
<section class="card article-head">
  <!-- ✅ 왼쪽 게시판명 배지 -->
  <div>
    <span class="badge primary">${boardLabel}</span>
  </div>

  <h1 class="title" style="margin:0;">${article.title}</h1>

  <div class="article-meta">
    <span>작성자 <b>${article.writerName}</b></span>
    <span>·</span>
    <span>작성일 <b>${article.regDate}</b></span>
    <span>·</span>
    <span>조회수 <b>${article.views}</b></span>
  </div>
</section>

<!-- ====== 본문 테이블 카드 ====== -->
<section class="card" style="margin-top:14px;">
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
      <td class="content-cell">
        <div class="content-text">${article.content}</div>
      </td>
    </tr>
  </table>
</section>

<!-- ====== 좋아요 카드 ====== -->
<section class="card" style="margin-top:14px;">
  <div class="like-section">
    <button type="button"
            id="articleLikeBtn"
            class="like-btn ${articleLiked ? 'liked' : ''}"
            data-liked="${articleLiked}"
            data-rel-type="article"
            data-rel-id="${article.id}">
      <span class="like-label">${articleLiked ? '좋아요 취소' : '좋아요'}</span>
    </button>

    <span style="color:var(--muted); font-weight:700;">
      좋아요 <b id="articleLikeCount" style="color:var(--text);">${articleLikeCount}</b>개
    </span>

    <div class="actions" style="margin-left:auto; margin-top:0;">
      <a class="btn" href="${ctx}/usr/article/list?boardId=${article.boardId}">목록으로</a>
      <c:if test="${canEdit}">
        <a class="btn" href="${ctx}/usr/article/modify?id=${article.id}">수정</a>
        <a class="btn" href="${ctx}/usr/article/delete?id=${article.id}&boardId=${article.boardId}"
           onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
      </c:if>
    </div>
  </div>
</section>

<!-- ====== 댓글 ====== -->
<section class="card reply-section">
  <div class="reply-head">
    <h2 style="margin:0; font-size:20px;">댓글</h2>
    <span class="badge">총 <b>${empty replyViews ? 0 : replyViews.size()}</b>개</span>
  </div>

  <c:if test="${empty replyViews}">
    <p class="muted">등록된 댓글이 없습니다.</p>
  </c:if>

  <c:if test="${not empty replyViews}">
    <div class="reply-list">
      <c:forEach var="item" items="${replyViews}">
        <c:set var="reply" value="${item.reply}" />
        <div class="reply-item" data-reply-id="${reply.id}">
          <div class="reply-meta">
            <span><b>${reply.writerName}</b></span>
            <span>${reply.regDate}</span>
          </div>

          <div class="reply-body">${reply.content}</div>

          <div class="reply-actions">
            <button type="button"
                    class="reply-like-btn ${item.liked ? 'liked' : ''}"
                    data-rel-type="reply"
                    data-rel-id="${reply.id}"
                    data-liked="${item.liked}">
              <span class="like-label">${item.liked ? '좋아요 취소' : '좋아요'}</span>
              <span class="count">${item.likeCount}</span>
            </button>

            <c:if test="${item.mine || req.admin}">
              <button type="button"
                      class="reply-delete-btn"
                      data-reply-id="${reply.id}">
                삭제
              </button>
            </c:if>
          </div>
        </div>
      </c:forEach>
    </div>
  </c:if>

  <c:choose>
    <c:when test="${not empty loginedMemberId}">
      <form class="reply-form" id="replyForm">
        <textarea id="replyContent" placeholder="댓글 내용을 입력해주세요."></textarea>
        <button type="submit" class="btn btn-primary" style="width:140px;">댓글 등록</button>
      </form>
    </c:when>
    <c:otherwise>
      <p class="muted">댓글을 작성하려면 로그인 해주세요.</p>
    </c:otherwise>
  </c:choose>
</section>

<script>
  const articleId = Number(document.body.dataset.articleId);
  const replyForm = document.getElementById('replyForm');
  const replyContent = document.getElementById('replyContent');

  function getResultCode(data) {
    if (!data) return '';
    return data.resultCode || data.rsCode || '';
  }

  function getResultMsg(data) {
    if (!data) return '';
    return data.msg || data.rsMsg || '';
  }

  function getResultPayload(data) {
    if (!data) return null;
    if (data.rsData !== undefined) return data.rsData;
    if (data.data !== undefined) return data.data;
    return null;
  }

  function isAuthErrorResult(data) {
    const code = getResultCode(data);
    return code === 'F-401' || code === 'F-403';
  }

  async function postForm(url, params) {
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept': 'application/json',
          'X-Requested-With': 'XMLHttpRequest'
        },
        body: new URLSearchParams(params).toString(),
        credentials: 'same-origin'
      });

      let data = null;
      try { data = await res.json(); } catch (e) {}

      if (!res.ok) {
        alert((data && getResultMsg(data)) || '요청 처리에 실패했습니다.');
        return null;
      }
      if (isAuthErrorResult(data)) {
        alert(getResultMsg(data) || '로그인이 필요합니다.');
        return null;
      }
      return data;
    } catch (err) {
      console.error(err);
      alert('요청 처리 중 오류가 발생했습니다.');
      return null;
    }
  }

  function handleResultData(data, defaultMsg) {
    if (!data) { alert(defaultMsg); return false; }
    if (isAuthErrorResult(data)) { alert(getResultMsg(data) || '로그인이 필요합니다.'); return false; }
    const code = getResultCode(data);
    if (code && code.startsWith('S-')) return true;
    alert(getResultMsg(data) || defaultMsg);
    return false;
  }

  function toggleLike(relType, relId, onSuccess) {
    postForm('/usr/likePoint/toggle', { relTypeCode: relType, relId })
      .then(data => {
        if (!data) return;
        if (!handleResultData(data, '좋아요 처리 중 문제가 발생했습니다.')) return;
        if (onSuccess) onSuccess(getResultPayload(data) || {});
        else window.location.reload();
      })
      .catch(err => {
        console.error(err);
        alert(err.message || '좋아요 처리 중 오류가 발생했습니다.');
      });
  }

  const articleLikeBtn = document.getElementById('articleLikeBtn');
  if (articleLikeBtn) {
    articleLikeBtn.addEventListener('click', () => {
      toggleLike('article', articleId, data => {
        const liked = !!data.liked;
        articleLikeBtn.dataset.liked = liked;
        articleLikeBtn.classList.toggle('liked', liked);
        const label = articleLikeBtn.querySelector('.like-label');
        if (label) label.textContent = liked ? '좋아요 취소' : '좋아요';

        const cntEl = document.getElementById('articleLikeCount');
        if (cntEl) cntEl.textContent = data.likeCount;
      });
    });
  }

  document.querySelectorAll('.reply-like-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const relId = btn.dataset.relId;
      toggleLike('reply', relId, data => {
        const liked = !!data.liked;
        btn.dataset.liked = liked;
        btn.classList.toggle('liked', liked);
        const label = btn.querySelector('.like-label');
        if (label) label.textContent = liked ? '좋아요 취소' : '좋아요';

        const countSpan = btn.querySelector('.count');
        if (countSpan) countSpan.textContent = data.likeCount;
      });
    });
  });

  document.querySelectorAll('.reply-delete-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      if (!confirm('댓글을 삭제하시겠습니까?')) return;
      const replyId = btn.dataset.replyId;

      postForm('/usr/reply/delete', { id: replyId })
        .then(data => {
          if (!data) return;
          if (!handleResultData(data, '댓글 삭제 처리 중 문제가 발생했습니다.')) return;
          window.location.reload();
        })
        .catch(err => {
          console.error(err);
          alert(err.message || '댓글 삭제 처리 중 오류가 발생했습니다.');
        });
    });
  });

  if (replyForm) {
    replyForm.addEventListener('submit', e => {
      e.preventDefault();
      const text = replyContent.value.trim();
      if (!text.length) {
        alert('댓글 내용을 입력해주세요.');
        replyContent.focus();
        return;
      }

      postForm('/usr/reply/write', {
        relTypeCode: 'article',
        relId: articleId,
        content: text
      }).then(data => {
        if (!data) return;
        if (!handleResultData(data, '댓글 등록 처리 중 문제가 발생했습니다.')) return;
        window.location.reload();
      }).catch(err => {
        console.error(err);
        alert(err.message || '댓글 등록 처리 중 오류가 발생했습니다.');
      });
    });
  }
</script>

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
