<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <title>회원가입 - My Memory Book</title>

  <style>
    .auth-wrap{ max-width:820px; margin:0 auto; }
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

    .join-form{ margin-top:18px; display:flex; flex-direction:column; gap:14px; }

    .grid-2{
      display:grid;
      grid-template-columns: 1fr 1fr;
      gap:12px;
    }
    @media (max-width: 720px){
      .grid-2{ grid-template-columns: 1fr; }
    }

    .field{ display:flex; flex-direction:column; gap:8px; }
    .label{
      font-weight:800;
      color:var(--text, #111);
      letter-spacing:-.2px;
    }

    /* mmb.css input 스타일을 그대로 쓰되, 폭/정렬만 보강 */
    .field input[type="text"],
    .field input[type="password"],
    .field input[type="email"],
    .field input[type="number"],
    .field select{
      width:100%;
    }

    .inline{
      display:flex;
      gap:10px;
      align-items:center;
    }
    .inline > *:first-child{ flex:1; }

    .hint{
      margin:0;
      font-size:12px;
      color:var(--muted);
      min-height:16px;
    }

    .divider{
      height:1px;
      background:rgba(0,0,0,.08);
      margin:4px 0;
    }

    .auth-actions{
      margin-top:6px;
      display:flex;
      gap:10px;
      justify-content:center;
      flex-wrap:wrap;
    }
    .auth-actions .btn{ min-width:120px; }

    .auth-links{
      margin-top:10px;
      display:flex;
      gap:14px;
      justify-content:center;
      color:var(--muted);
      font-weight:700;
    }
    .auth-links a:hover{ color:var(--primary); }
  </style>

  <script>
    // 아이디 중복 체크
    function checkId() {
      const username = document.getElementById("username").value.trim();
      if (username.length < 4) {
        alert("아이디는 4자 이상 입력해주세요.");
        return;
      }

      fetch('/usr/member/checkUsername?username=' + encodeURIComponent(username))
        .then(res => res.json())
        .then(data => {
          if (data.result === 'success') {
            alert("사용 가능한 아이디입니다.");
            document.getElementById("idChecked").value = "Y";
          } else {
            alert("이미 사용중인 아이디입니다.");
            document.getElementById("idChecked").value = "N";
          }
        });
    }

    // 닉네임 중복 체크 (비동기)
    function checkNickname() {
      const nicknameInput = document.getElementById("nickname");
      const nicknameMsg = document.getElementById("nicknameMsg");
      const nicknameChecked = document.getElementById("nicknameChecked");
      const nickname = nicknameInput.value.trim();

      if (nickname.length === 0) {
        nicknameMsg.textContent = "";
        nicknameChecked.value = "N";
        return;
      }

      fetch('/usr/member/checkNickname?nickname=' + encodeURIComponent(nickname))
        .then(res => res.json())
        .then(data => {
          if (data.result === 'fail') {
            nicknameMsg.textContent = "중복된 닉네임";
            nicknameMsg.style.color = "red";
            nicknameChecked.value = "N";
          } else {
            nicknameMsg.textContent = "";
            nicknameChecked.value = "Y";
          }
        });
    }

    // 비밀번호 확인 표시
    function checkPasswordMatch() {
      const pw = document.getElementById("password").value;
      const pwConfirm = document.getElementById("passwordConfirm").value;
      const msg = document.getElementById("passwordMsg");

      if (pwConfirm.length === 0) {
        msg.textContent = "";
        return;
      }

      if (pw === pwConfirm) {
        msg.textContent = "";
      } else {
        msg.textContent = "비밀번호가 일치하지 않습니다.";
        msg.style.color = "red";
      }
    }

    // 최종 검증 + ✅ 중복 제출 방지
    function validateForm(form) {
      if (document.getElementById("idChecked").value !== "Y") {
        alert("아이디 중복 체크를 완료해주세요.");
        return false;
      }
      if (document.getElementById("nicknameChecked").value !== "Y") {
        alert("닉네임 중복 체크를 완료해주세요.");
        return false;
      }

      const pw = document.getElementById("password").value;
      const pwConfirm = document.getElementById("passwordConfirm").value;
      if (pw !== pwConfirm) {
        alert("비밀번호가 일치하지 않습니다.");
        return false;
      }

      // ✅ 제출 버튼 잠금
      const btn = document.getElementById("submitBtn");
      if (btn) {
        btn.disabled = true;
        btn.textContent = "가입 처리중...";
      }
      return true;
    }
  </script>
</head>

<body>
<%@ include file="/view/usr/common/header.jsp" %>

<main class="page">
  <div class="container">
    <div class="auth-wrap">
      <div class="card auth-card">
        <h1 class="auth-title">회원가입</h1>
        <p class="auth-sub">My Memory Book과 함께 단어 습관을 시작해요.</p>

        <form class="join-form" action="/usr/member/doJoin" method="post" onsubmit="return validateForm(this);">
          <input type="hidden" id="idChecked" value="N">
          <input type="hidden" id="nicknameChecked" value="N">

          <div class="grid-2">
            <div class="field">
              <div class="label">아이디</div>
              <div class="inline">
                <input type="text" id="username" name="username" required placeholder="4자 이상">
                <button class="btn" type="button" onclick="checkId()">중복확인</button>
              </div>
              <p class="hint">아이디 중복확인을 먼저 해주세요.</p>
            </div>

            <div class="field">
              <div class="label">이메일</div>
              <input type="email" name="email" required placeholder="example@email.com">
              <p class="hint">비밀번호 찾기 등에 사용돼요.</p>
            </div>
          </div>

          <div class="grid-2">
            <div class="field">
              <div class="label">비밀번호</div>
              <input type="password" id="password" name="password" required oninput="checkPasswordMatch()" placeholder="비밀번호">
            </div>

            <div class="field">
              <div class="label">비밀번호 확인</div>
              <input type="password" id="passwordConfirm" required oninput="checkPasswordMatch()" placeholder="비밀번호 확인">
              <p id="passwordMsg" class="hint" style="color:red; height:16px;"></p>
            </div>
          </div>

          <div class="divider"></div>

          <div class="grid-2">
            <div class="field">
              <div class="label">이름</div>
              <input type="text" name="name" required placeholder="이름">
            </div>

            <div class="field">
              <div class="label">나이</div>
              <input type="number" name="age" min="1" max="100" placeholder="선택 입력">
              <p class="hint">선택 입력이에요.</p>
            </div>
          </div>

          <div class="grid-2">
            <div class="field">
              <div class="label">닉네임</div>
              <input
                type="text"
                id="nickname"
                name="nickname"
                required
                onblur="checkNickname()"
                oninput="document.getElementById('nicknameChecked').value='N'; document.getElementById('nicknameMsg').textContent='';"
                placeholder="닉네임"
              >
              <p id="nicknameMsg" class="hint" style="color:red; height:16px;"></p>
            </div>

            <div class="field">
              <div class="label">거주지역</div>
              <select name="region">
                <option value="서울">서울</option>
                <option value="경기">경기</option>
                <option value="부산">부산</option>
                <option value="대구">대구</option>
                <option value="인천">인천</option>
                <option value="광주">광주</option>
                <option value="대전">대전</option>
                <option value="울산">울산</option>
                <option value="강원">강원</option>
                <option value="경북">경북</option>
                <option value="경남">경남</option>
                <option value="충북">충북</option>
                <option value="충남">충남</option>
                <option value="전북">전북</option>
                <option value="전남">전남</option>
                <option value="제주">제주</option>
                <option value="기타">기타</option>
              </select>
            </div>
          </div>

          <div class="field">
            <div class="label">일일 학습 목표량</div>
            <select name="dailyTarget">
              <option value="10">10개</option>
              <option value="30" selected>30개</option>
              <option value="50">50개</option>
              <option value="70">70개</option>
              <option value="100">100개</option>
            </select>
            <p class="hint">나중에 마이페이지에서 언제든 바꿀 수 있어요.</p>
          </div>

          <div class="auth-actions">
            <button id="submitBtn" class="btn btn-primary" type="submit">가입하기</button>
            <button class="btn btn-ghost" type="button" onclick="history.back();">취소</button>
          </div>

          <div class="auth-links">
            <a href="/usr/member/login">이미 계정이 있어요 (로그인)</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</main>

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
