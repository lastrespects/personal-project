<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>아이디 찾기</title>
    <script>
        function findLoginId() {
            const nameInput = document.getElementById("name");
            const emailInput = document.getElementById("email");
            const name = nameInput.value.trim();
            const email = emailInput.value.trim();

            if (name.length === 0) {
                alert("이름을 입력해주세요.");
                nameInput.focus();
                return;
            }
            if (email.length === 0) {
                alert("이메일을 입력해주세요.");
                emailInput.focus();
                return;
            }

            const url = '/usr/member/doFindLoginId?name=' + encodeURIComponent(name) + '&email=' + encodeURIComponent(email);
            fetch(url)
                .then(res => res.json())
                .then(data => {
                    alert(data.rsMsg || '처리 중 오류가 발생했습니다.');
                    if (data.rsCode && data.rsCode.startsWith('S-')) {
                        window.location.href = "/login";
                    }
                })
                .catch(() => alert('아이디 찾기 중 오류가 발생했습니다.'));
        }
    </script>
</head>
<body>
    <h1>아이디 찾기</h1>
    <div style="width:300px; margin-top:20px;">
        <p>이름</p>
        <input id="name" type="text" style="width:100%;">
        <p style="margin-top:12px;">이메일</p>
        <input id="email" type="email" style="width:100%;">
        <div style="margin-top:16px;">
            <button onclick="findLoginId()">아이디 찾기</button>
            <button onclick="history.back();" style="margin-left:8px;">뒤로가기</button>
        </div>
        <div style="margin-top:10px;">
            <a href="/usr/member/findLoginPw">비밀번호 찾기</a> |
            <a href="/usr/member/login">로그인</a>
        </div>
    </div>
</body>
</html>
