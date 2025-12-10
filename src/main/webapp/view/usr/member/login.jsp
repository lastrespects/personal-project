<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인 - My Memory Book</title>
<script>
    // 페이지 로드 시 msg 파라미터가 있으면 alert 표시
    window.onload = function() {
        const urlParams = new URLSearchParams(window.location.search);
        const msg = urlParams.get('msg');
        if (msg) {
            alert(decodeURIComponent(msg));
        }
    };
    function loginFormSubmit(form) {
        const usernameInput = form.username;
        const passwordInput = form.password;
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (username.length === 0) {
            alert("아이디를 입력해주세요.");
            usernameInput.focus();
            return false;
        }

        if (password.length === 0) {
            alert("비밀번호를 입력해주세요.");
            passwordInput.focus();
            return false;
        }

        fetch('/usr/member/validLoginInfo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ username, password })
        })
            .then(res => res.json())
            .then(data => {
                if (data.rsCode) {
                    if (data.rsCode.startsWith('F-')) {
                        alert(data.rsMsg || '로그인 정보가 올바르지 않습니다.');
                        return;
                    }
                    if (data.rsCode === 'D-1') {
                        const restoreUntil = data.data && data.data.restoreUntil ? data.data.restoreUntil : '';
                        const confirmRestore = confirm(`탈퇴한 계정입니다.\n${restoreUntil ? restoreUntil + ' 이후 ' : ''}복구하시겠습니까?`);
                        if (!confirmRestore) {
                            return;
                        }
                        return fetch('/usr/member/restore', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: new URLSearchParams({ username, password })
                        })
                        .then(res2 => res2.json())
                        .then(res2 => {
                            if (!res2.rsCode || !res2.rsCode.startsWith('S-')) {
                                alert(res2.rsMsg || '계정 복구에 실패했습니다.');
                                return;
                            }
                            alert('계정을 복구했습니다. 다시 로그인합니다.');
                            form.submit();
                        })
                        .catch(() => alert('복구 요청 중 오류가 발생했습니다.'));
                    }
                }

                usernameInput.value = username;
                passwordInput.value = password;
                form.submit();
            })
            .catch(() => {
                alert('로그인 요청 중 오류가 발생했습니다.');
            });

        return false;
    }
</script>
</head>
<body>
    <div style="text-align:center; margin-top:100px;">
        <h1>로그인</h1>
        <form action="/doLogin" method="post" onsubmit="return loginFormSubmit(this);">
            <input type="text" name="username" placeholder="아이디" required><br><br>
            <input type="password" name="password" placeholder="비밀번호" required><br><br>
            <button type="submit">로그인</button>
            <button type="button" onclick="location.href='/usr/member/join'">회원가입</button>
            <button type="button" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
        </form>
        <div style="margin-top:12px;">
            <a href="/usr/member/findLoginId" style="margin-right:10px;">아이디 찾기</a>
            <a href="/usr/member/findLoginPw">비밀번호 찾기</a>
        </div>
    </div>
</body>
</html>
