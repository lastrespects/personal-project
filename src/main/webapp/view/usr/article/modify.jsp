<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
    <title>글 수정</title>

    <style>
        /* 이 페이지에서만 필요한 "배치"만 최소로 */
        .form-wrap{ max-width: 860px; margin: 0 auto; }
        .form-card{ padding: 22px; }
        .form-title{ margin: 0 0 14px; font-size: 22px; font-weight: 900; letter-spacing: -0.4px; }
        .form-grid{ display: grid; gap: 12px; }
        .form-label{ font-weight: 800; color: var(--text); }
        .actions{ margin-top: 14px; display:flex; gap:10px; justify-content:flex-end; }
        textarea{ min-height: 340px; } /* 내용칸 좀 더 크게 */
    </style>
</head>

<body>
<%@ include file="/view/usr/common/header.jsp" %>

<div class="form-wrap">
    <div class="card form-card">
        <h1 class="form-title">글 수정</h1>

        <form action="/usr/article/doModify" method="post" class="form-grid">
            <input type="hidden" name="id" value="${article.id}" />

            <div class="form-grid">
                <label class="form-label">제목</label>
                <input type="text" name="title" required value="${article.title}" />
            </div>

            <div class="form-grid">
                <label class="form-label">내용</label>
                <textarea name="content" required>${article.content}</textarea>
            </div>

            <div class="actions">
                <a class="btn btn-ghost" href="javascript:history.back();">취소</a>
                <button class="btn btn-primary" type="submit">저장</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
