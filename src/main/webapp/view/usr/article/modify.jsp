<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>글 수정</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { margin-bottom: 12px; }
        form { max-width: 720px; }
        label { display: block; margin-top: 12px; font-weight: bold; }
        input[type="text"], textarea { width: 100%; padding: 10px; border: 1px solid #ccc; }
        textarea { height: 240px; resize: vertical; }
        .actions { margin-top: 14px; display: flex; gap: 8px; }
        .actions button, .actions a { padding: 8px 12px; border: 1px solid #333; background: #fff; text-decoration: none; }
    </style>
</head>
<body>
    <h1>글 수정</h1>
    <form action="/usr/article/doModify" method="post">
        <input type="hidden" name="id" value="${article.id}" />
        <label>제목</label>
        <input type="text" name="title" required value="${article.title}" />

        <label>내용</label>
        <textarea name="content" required>${article.content}</textarea>

        <div class="actions">
            <button type="submit">저장</button>
            <a href="javascript:history.back();">취소</a>
        </div>
    </form>
</body>
</html>
