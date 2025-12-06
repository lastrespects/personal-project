<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>鍮꾨?踰덊샇 李얘린</title>
    <script>
        function findLoginPw() {
            const usernameInput = document.getElementById("loginId");
            const emailInput = document.getElementById("email");
            const loginId = usernameInput.value.trim();
            const email = emailInput.value.trim();

            if (loginId.length === 0) {
                alert("?꾩씠?붾? ?낅젰?댁＜?몄슂.");
                usernameInput.focus();
                return;
            }
            if (email.length === 0) {
                alert("?대찓?쇱쓣 ?낅젰?댁＜?몄슂.");
                emailInput.focus();
                return;
            }

            const btn = document.getElementById("findBtn");
            btn.disabled = true;

            const url = '/usr/member/doFindLoginPw?loginId=' + encodeURIComponent(loginId) + '&email=' + encodeURIComponent(email);
            fetch(url)
                .then(res => res.json())
                .then(data => {
                    alert(data.rsMsg || "처리 중 오류가 발생했습니다.");
                    if (data.rsCode && data.rsCode.startsWith('S-')) {
                        window.location.href = "/login";
                    }
                })
                .catch(() => alert('鍮꾨?踰덊샇 李얘린 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.'))
                .finally(() => { btn.disabled = false; });
        }
    </script>
</head>
<body>
    <h1>鍮꾨?踰덊샇 李얘린</h1>
    <div style="width:300px; margin-top:20px;">
        <p>?꾩씠??/p>
        <input id="loginId" type="text" style="width:100%;">
        <p style="margin-top:12px;">?대찓??/p>
        <input id="email" type="email" style="width:100%;">
        <div style="margin-top:16px;">
            <button id="findBtn" onclick="findLoginPw()">鍮꾨?踰덊샇 李얘린</button>
            <button onclick="history.back();" style="margin-left:8px;">?ㅻ줈媛湲?/button>
        </div>
        <div style="margin-top:10px;">
            <a href="/usr/member/findLoginId">?꾩씠??李얘린</a> |
            <a href="/usr/member/login">濡쒓렇??/a>
        </div>
    </div>
</body>
</html>

