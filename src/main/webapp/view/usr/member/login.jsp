<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <title>로그인 - My Memory Book</title>

  <style>
    .auth-wrap{ max-width:720px; margin:0 auto; }
    .auth-card{ padding:26px; }
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
    // ✅ 페이지 로드 시 msg 파라미터가 있으면 alert 표시 (한 번만)
    window.onload = function () {
      const url = new URL(window.location.href);
      const msg = url.searchParams.get('msg'); // URLSearchParams는 기본적으로 디코딩된 값으로 줌

      if (msg && msg.trim().length > 0) {
        alert(msg); // ✅ decodeURIComponent 다시 하지 말 것(중복 디코딩 위험)

        // ✅ 새로고침해도 계속 뜨지 않도록 msg 제거
        url.searchParams.delete('msg');
        history.replaceState(null, '', url.pathname + (url.searchParams.toString() ? '?' + url.searchParams.toString() : ''));
      }
    };

    function pickRestoreUntil(res) {
      const root = res || {};
      const rsData = root.rsData || root.data || (root.resultData && root.resultData.rsData) || {};
      const v = rsData.restoreUntil ?? rsData.restoreUntilStr ?? rsData.restoreDate ?? rsData.restore_until ?? "";
      const s = String(v ?? "").trim();
      return s.length > 0 ? s : "";
    }

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
            const restoreData = data.rsData || data.data || {};
            const restoreUntil = pickRestoreUntil(data);
            const baseMsg = String(data.rsMsg || data.msg || '').trim();
            const fallbackMsg = '7일 이내에 같은 계정으로 로그인하면 복구할 수 있습니다.';
            const restoreNotice = restoreUntil.length > 0
              ? `${restoreUntil}까지 같은 계정으로 로그인하면 복구할 수 있습니다.`
              : (baseMsg.length > 0 ? baseMsg : fallbackMsg);

            const nickname = restoreData.nickname ? restoreData.nickname : '';
            const welcomeName = nickname ? `${nickname}님, ` : '';
            const confirmRestore = confirm(`${welcomeName}탈퇴 상태의 계정입니다.\n${restoreNotice}\n바로 복구를 진행하시겠습니까?`);
            if (!confirmRestore) return;

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
<%@ include file="/view/usr/common/header.jsp" %>

<main class="page">
  <div class="container">
    <div class="auth-wrap">
      <div class="card auth-card">
        <h1 class="auth-title">로그인</h1>
        <p class="auth-sub">My Memory Book에 오신 걸 환영해요.</p>

        <form class="auth-form" action="/doLogin" method="post" onsubmit="return loginFormSubmit(this);">
          <input type="text" name="username" placeholder="아이디" autocomplete="username" required>
          <input type="password" name="password" placeholder="비밀번호" autocomplete="current-password" required>

          <div class="auth-actions">
            <button class="btn btn-primary" type="submit">로그인</button>
            <button class="btn" type="button" onclick="location.href='/usr/member/join'">회원가입</button>
            <button class="btn btn-ghost" type="button" onclick="location.href='/usr/home/main'">메인으로</button>
          </div>

          <div class="auth-links">
            <a href="/usr/member/findLoginId">아이디 찾기</a>
            <span style="opacity:.35;">|</span>
            <a href="/usr/member/findLoginPw">비밀번호 찾기</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</main>

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
