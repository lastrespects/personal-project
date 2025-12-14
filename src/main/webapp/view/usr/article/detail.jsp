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
        .actions { margin-top: 14px; display: flex; gap: 8px; flex-wrap: wrap; }
        .actions a { padding: 6px 10px; border: 1px solid #333; text-decoration: none; font-size: 13px; background: #fff; }
        .content { line-height: 1.6; white-space: pre-wrap; }
        .like-section { margin-top: 20px; display: flex; align-items: center; gap: 8px; }
        .like-btn { padding: 6px 12px; border: 1px solid #f97316; background: #fff5ee; color: #c2410c; border-radius: 6px; cursor: pointer; }
        .like-btn.liked { background: #ffedd5; color: #b45309; font-weight: bold; }
        .reply-section { margin-top: 32px; }
        .reply-item { border: 1px solid #e5e7eb; border-radius: 6px; padding: 10px; margin-bottom: 10px; }
        .reply-meta { font-size: 13px; color: #6b7280; display: flex; justify-content: space-between; }
        .reply-body { margin: 8px 0; white-space: pre-wrap; }
        .reply-actions { display: flex; gap: 10px; font-size: 13px; }
        .reply-actions button { border: none; background: none; padding: 0; color: #2563eb; cursor: pointer; }
        .reply-actions button.danger { color: #dc2626; }
        .reply-form textarea { width: 100%; min-height: 80px; padding: 10px; border-radius: 6px; border: 1px solid #d1d5db; resize: vertical; }
        .reply-form button { margin-top: 8px; padding: 8px 14px; border: none; border-radius: 6px; background: #2563eb; color: #fff; cursor: pointer; }
        .muted { color: #9ca3af; font-size: 14px; margin-top: 8px; }
    </style>
</head>
<body data-article-id="${article.id}">
<c:set var="canEdit" value="${canEdit}" />

<h1>${article.title}</h1>
<div class="meta">
    작성자 ${article.writerName} | 작성일 ${article.regDate} | 조회수 ${article.views}
</div>

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

<div class="like-section">
    <button type="button"
            id="articleLikeBtn"
            class="like-btn ${articleLiked ? 'liked' : ''}"
            data-liked="${articleLiked}"
            data-rel-type="article"
            data-rel-id="${article.id}">
        <span class="like-label">${articleLiked ? '좋아요 취소' : '좋아요'}</span>
    </button>
    <span>좋아요 <strong id="articleLikeCount">${articleLikeCount}</strong>개</span>
</div>

<div class="actions">
    <a href="/usr/article/list?boardId=${article.boardId}">목록으로</a>
    <c:if test="${canEdit}">
        <a href="/usr/article/modify?id=${article.id}">수정</a>
        <a href="/usr/article/delete?id=${article.id}&boardId=${article.boardId}"
           onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
    </c:if>
</div>

<section class="reply-section">
    <h2>댓글</h2>
    <c:if test="${empty replyViews}">
        <p class="muted">등록된 댓글이 없습니다.</p>
    </c:if>
    <c:forEach var="item" items="${replyViews}">
        <c:set var="reply" value="${item.reply}" />
        <div class="reply-item" data-reply-id="${reply.id}">
            <div class="reply-meta">
                <span>${reply.writerName}</span>
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
                            class="danger reply-delete-btn"
                            data-reply-id="${reply.id}">
                        삭제
                    </button>
                </c:if>
            </div>
        </div>
    </c:forEach>

    <c:choose>
        <c:when test="${not empty loginedMemberId}">
            <form class="reply-form" id="replyForm">
                <textarea id="replyContent" placeholder="댓글 내용을 입력해주세요."></textarea>
                <button type="submit">댓글 등록</button>
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
        if (data.rsData !== undefined) {
            return data.rsData;
        }
        if (data.data !== undefined) {
            return data.data;
        }
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
            try {
                data = await res.json();
            } catch (e) {
                // ignore json parse failure
            }
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
        if (!data) {
            alert(defaultMsg);
            return false;
        }
        if (isAuthErrorResult(data)) {
            alert(getResultMsg(data) || '로그인이 필요합니다.');
            return false;
        }
        const code = getResultCode(data);
        if (code && code.startsWith('S-')) {
            return true;
        }
        alert(getResultMsg(data) || defaultMsg);
        return false;
    }

    function toggleLike(relType, relId, onSuccess) {
        postForm('/usr/likePoint/toggle', { relTypeCode: relType, relId })
            .then(data => {
                if (!data) {
                    return;
                }

                if (!handleResultData(data, '좋아요 처리 중 문제가 발생했습니다.')) {
                    return;
                }
                if (onSuccess) {
                    onSuccess(getResultPayload(data) || {});
                } else {
                    window.location.reload();
                }
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
                if (label) {
                    label.textContent = liked ? '좋아요 취소' : '좋아요';
                }
                const cntEl = document.getElementById('articleLikeCount');
                if (cntEl) {
                    cntEl.textContent = data.likeCount;
                }
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
                if (label) {
                    label.textContent = liked ? '좋아요 취소' : '좋아요';
                }
                const countSpan = btn.querySelector('.count');
                if (countSpan) {
                    countSpan.textContent = data.likeCount;
                }
            });
        });
    });

    document.querySelectorAll('.reply-delete-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!confirm('댓글을 삭제하시겠습니까?')) {
                return;
            }
            const replyId = btn.dataset.replyId;
            postForm('/usr/reply/delete', { id: replyId })
                .then(data => {
                    if (!data) {
                        return;
                    }
                    if (!handleResultData(data, '댓글 삭제 처리 중 문제가 발생했습니다.')) {
                        return;
                    }
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
                if (!data) {
                    return;
                }

                if (!handleResultData(data, '댓글 등록 처리 중 문제가 발생했습니다.')) {
                    return;
                }
                window.location.reload();
            }).catch(err => {
                console.error(err);
                alert(err.message || '댓글 등록 처리 중 오류가 발생했습니다.');
            });
        });
    }
</script>
</body>
</html>
