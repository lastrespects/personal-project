<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인 - My Memory Book</title>
</head>
<body>
    <div style="text-align:center; margin-top:100px;">
        <h1>🔐 로그인</h1>
        <form action="/doLogin" method="post">
            <input type="text" name="username" placeholder="아이디" required><br><br>
            <input type="password" name="password" placeholder="비밀번호" required><br><br>
            <button type="submit">로그인</button>
            <button type="button" onclick="location.href='/usr/member/join'">회원가입</button>
        </form>
    </div>
</body>
</html>