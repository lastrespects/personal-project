<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
    <title>비밀번호 찾기</title>

    <style>
        /* ✅ 디자인만 추가 */
        .auth-wrap{ max-width: 720px; margin: 0 auto; }
        .auth-card{ padding: 26px; }
        .auth-title{
            margin:0;
            font-size:32px;
            font-weight:900;
            letter-spacing:-.6px;
            text-align:center;
        }
        .auth-sub{
            margin:10px 0 0;
            color:var(--muted);
            font-weight:700;
            text-align:center;
        }

        .auth-form{ margin-top:18px; display:flex; flex-direction:column; gap:12px; }

        .field{ display:flex; flex-direction:column; gap:6px; }
        .field label{
            font-weight:800;
            color:var(--text);
            font-size:14px;
            letter-spacing:-.2px;
        }
        .auth-form input{ width:100%; }

        .auth-actions{
            margin-top:10px;
            display:flex;
            gap:10px;
            justify-content:center;
            flex-wrap:wrap;
        }

        .auth-links{
            margin-top:14px;
            display:flex;
            gap:14px;
            justify-content:center;
            color:var(--muted);
            font-weight:700;
        }
        .auth-links a:hover{ color:var(--primary); }
    </style>

    <script>
        function findLoginPw() {
            const usernameInput = document.getElementById("loginId");
            const emailInput = document.getElementById("email");
            const loginId = usernameInput.value.trim();
            const email = emailInput.value.trim();

            if (loginId.length === 0) {
                alert("아이디를 입력해주세요.");
                usernameInput.focus();
                return;
            }
            if (email.length === 0) {
                alert("이메일을 입력해주세요.");
                emailInput.focus();
                return;
            }

            const btn = document.getElementById("findBtn");
            btn.disabled = true;

            const url = '/usr/member/doFindLoginPw?loginId=' + encodeURIComponent(loginId) + '&email=' + encodeURIComponent(email);
            fetch(url)
                .then(res => res.json())
                .then(data => {
                    alert(data.rsMsg || "처리 중 문제가 발생했습니다.");
                    if (data.rsCode && data.rsCode.startsWith('S-')) {
                        window.location.href = "/login";
                    }
                })
                .catch(() => alert('비밀번호 찾기 처리 중 오류가 발생했습니다.'))
                .finally(() => { btn.disabled = false; });
        }
    </script>
</head>
<body>
    <%@ include file="/view/usr/common/header.jsp" %>

    <main class="page">
        <div class="container">
            <div class="auth-wrap">
                <div class="card auth-card">
                    <h1 class="auth-title">비밀번호 찾기</h1>
                    <p class="auth-sub">임시 비밀번호를 발급받을 계정 정보를 입력해 주세요.</p>

                    <!-- ✅ form submit 없이 기존 onclick 로직 그대로 사용 -->
                    <div class="auth-form">
                        <div class="field">
                            <label for="loginId">아이디</label>
                            <input id="loginId" type="text" placeholder="아이디를 입력하세요">
                        </div>

                        <div class="field">
                            <label for="email">이메일</label>
                            <input id="email" type="email" placeholder="example@email.com">
                        </div>

                        <div class="auth-actions">
                            <button class="btn btn-primary" id="findBtn" type="button" onclick="findLoginPw()">비밀번호 찾기</button>
                            <button class="btn" type="button" onclick="history.back();" style="margin-left:0;">뒤로가기</button>
                        </div>

                        <div class="auth-links">
                            <a href="/usr/member/findLoginId">아이디 찾기</a>
                            <span style="opacity:.35;">|</span>
                            <a href="/usr/member/login">로그인</a>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </main>

    <%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
