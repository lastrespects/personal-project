<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <title>마이페이지</title>
  <style>
    body {
      font-family: "Noto Sans KR", Arial, sans-serif;
      background: #f7f8fb;
      margin: 0;
    }

    /* 페이지 래퍼 */
    .page-wrap {
      width: min(1100px, 92vw);
      margin: 0 auto;
      padding: 28px 0 56px;
    }

    .page-title {
      font-size: 28px;
      font-weight: 800;
      letter-spacing: -0.2px;
      margin: 6px 0 18px;
    }

    /* 2컬럼 레이아웃 */
    .grid {
      display: grid;
      grid-template-columns: 360px 1fr;
      gap: 18px;
      align-items: start;
    }

    @media (max-width: 980px) {
      .grid {
        grid-template-columns: 1fr;
      }
    }

    /* 카드 공통 */
    .card {
      width: auto; /* ✅ 기존 460px 고정 제거(디자인만) */
      border: 1px solid #e6e8ef;
      background: #fff;
      border-radius: 16px;
      padding: 18px;
      margin-top: 0; /* ✅ 그리드에서 여백 관리 */
      box-shadow: 0 10px 26px rgba(16, 24, 40, 0.06);
    }

    .card h3 {
      margin: 0 0 12px;
      font-size: 18px;
      font-weight: 800;
      letter-spacing: -0.2px;
    }

    .divider {
      height: 1px;
      background: #eef0f5;
      margin: 14px 0;
    }

    /* 왼쪽 요약 정보 */
    .kv {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      padding: 10px 12px;
      border: 1px dashed #e6e8ef;
      border-radius: 12px;
      margin-bottom: 10px;
      background: #fbfcff;
    }

    .kv .k {
      color: #6b7280;
      font-weight: 700;
    }

    .kv .v {
      color: #111827;
      font-weight: 800;
    }

    /* 폼 스타일 */
    .row {
      margin-top: 14px; /* ✅ 기존 10px에서 조금 넓힘(디자인만) */
    }

    .label {
      margin: 0 0 8px;
      font-weight: 800;
      color: #111827;
    }

    .msg {
      font-size: 12px;
      min-height: 16px;
      margin: 8px 0 0;
      color: #6b7280;
    }

    input[type="text"],
    input[type="email"],
    input[type="password"],
    select {
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 12px 14px;
      outline: none;
      background: #fff;
      transition: box-shadow .15s ease, border-color .15s ease;
    }

    input[type="text"]:focus,
    input[type="email"]:focus,
    input[type="password"]:focus,
    select:focus {
      border-color: #4f7cff;
      box-shadow: 0 0 0 4px rgba(79, 124, 255, 0.14);
    }

    /* 닉네임 입력 + 중복확인 버튼 줄 */
    .nickname-line {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .nickname-line input[type="text"] {
      flex: 1;
    }

    /* 버튼 공통 */
    button {
      cursor: pointer;
      border: 1px solid transparent;
      border-radius: 12px;
      padding: 10px 14px;
      font-weight: 800;
      transition: transform .05s ease, box-shadow .15s ease, background .15s ease, border-color .15s ease;
      user-select: none;
      background: #2563eb;
      color: #fff;
      box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22);
    }

    button:active {
      transform: translateY(1px);
    }

    /* 중복확인/취소같은 보조 버튼 */
    .btn-outline {
      background: #fff;
      color: #1f2937;
      border-color: #d7def1;
      box-shadow: none;
    }

    .btn-outline:hover {
      border-color: #bfc9ea;
      background: #fbfcff;
    }

    /* 탈퇴 버튼 */
    .btn-danger {
      background: #fff;
      color: #dc2626;
      border-color: #fecaca;
      box-shadow: none;
    }

    .btn-danger:hover {
      background: #fff5f5;
      border-color: #fca5a5;
    }

    /* 하단 액션 정렬 */
    .actions-right {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      flex-wrap: wrap;
    }

    .withdraw-wrap {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  </style>

  <script>
    const nicknameChangeAllowed = ${ nicknameChangeAllowed ?"true": "false"};
    const nextNicknameChangeDate = "${nextNicknameChangeDate}";
    const nicknameDaysLeft = ${ nicknameDaysLeft != null ? nicknameDaysLeft : 0};

    function pickRestoreUntil(res) {
      const root = res || {};
      const rsData = root.rsData || root.data || (root.resultData && root.resultData.rsData) || {};
      const v = rsData.restoreUntil ?? rsData.restoreUntilStr ?? rsData.restoreDate ?? rsData.restore_until ?? "";
      const s = String(v ?? "").trim();
      return s.length > 0 ? s : "";
    }

    function setMsg(id, text, color) {
      const el = document.getElementById(id);
      if (!el) return;
      el.textContent = text || "";
      el.style.color = color || "";
    }

    function updateNicknameNotice() {
      if (!document.getElementById("nicknameNotice")) return;
      if (nicknameChangeAllowed) {
        setMsg("nicknameNotice", "닉네임은 변경 후 30일 동안 다시 변경할 수 없습니다.", "#777");
      } else if (nextNicknameChangeDate) {
        const info = nicknameDaysLeft > 0
          ? `${nicknameDaysLeft}일 후, ${nextNicknameChangeDate} 이후 변경 가능`
          : `${nextNicknameChangeDate} 이후 변경 가능`;
        setMsg("nicknameNotice", info, "#c00");
      } else {
        setMsg("nicknameNotice", "닉네임 변경이 현재 제한되어 있습니다.", "#c00");
      }
    }

    function checkPasswordMatch() {
      const pw = document.getElementById("newPassword").value;
      const pwConfirm = document.getElementById("newPasswordConfirm").value;
      if ((pw.length === 0 && pwConfirm.length === 0) || pw === pwConfirm) {
        setMsg("passwordChangeMsg", "", "");
        return true;
      }
      setMsg("passwordChangeMsg", "비밀번호 확인이 일치하지 않습니다.", "red");
      return false;
    }

    async function checkNicknameAvailability(nickname) {
      const res = await fetch("/usr/member/checkNickname?nickname=" + encodeURIComponent(nickname))
        .then(r => r.json())
        .catch(() => null);
      return res && res.result === "success";
    }

    async function handleNicknameCheck() {
      const nickname = document.getElementById("nickname").value.trim();
      const original = document.getElementById("nicknameOriginal").value;
      if (!nickname) {
        setMsg("nicknameStatus", "닉네임을 입력해주세요.", "red");
        return;
      }
      if (!nicknameChangeAllowed && nickname !== original) {
        setMsg("nicknameStatus", "아직 닉네임을 변경할 수 없습니다.", "red");
        return;
      }
      const ok = await checkNicknameAvailability(nickname);
      if (ok) {
        setMsg("nicknameStatus", "사용 가능한 닉네임입니다.", "#52c41a");
      } else {
        setMsg("nicknameStatus", "이미 사용 중인 닉네임입니다.", "red");
      }
    }

    async function saveProfile(event) {
      event.preventDefault();
      const nickname = document.getElementById("nickname").value.trim();
      const originalNickname = document.getElementById("nicknameOriginal").value;
      const email = document.getElementById("email").value.trim();
      const region = document.getElementById("region").value.trim();
      const dailyTarget = document.getElementById("dailyTarget").value;
      const pw = document.getElementById("newPassword").value;
      const pwConfirm = document.getElementById("newPasswordConfirm").value;

      setMsg("profileMsg", "", "");
      setMsg("passwordChangeMsg", "", "");
      setMsg("nicknameStatus", "", "");

      if (!nickname) {
        setMsg("profileMsg", "닉네임을 입력해주세요.", "red");
        return false;
      }

      const nicknameChanged = nickname !== originalNickname;
      if (nicknameChanged && !nicknameChangeAllowed) {
        setMsg("profileMsg", "아직 닉네임을 변경할 수 없습니다.", "red");
        return false;
      }
      if (nicknameChanged) {
        const ok = await checkNicknameAvailability(nickname);
        if (!ok) {
          setMsg("profileMsg", "이미 사용 중인 닉네임입니다.", "red");
          return false;
        }
      }

      if (!checkPasswordMatch()) return false;

      // 프로필 변경
      const profileRes = await fetch("/usr/member/modifyProfile", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ nickname, email, region, dailyTarget })
      }).then(res => res.json()).catch(() => null);

      if (!profileRes || !(profileRes.rsCode && profileRes.rsCode.startsWith("S-"))) {
        setMsg("profileMsg", (profileRes && profileRes.rsMsg) ? profileRes.rsMsg : "프로필 저장 중 오류가 발생했습니다.", "red");
        return false;
      }

      // 비밀번호 변경 (입력한 경우만)
      if (pw.length > 0) {
        const pwRes = await fetch("/usr/member/modifyPassword", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({ password: pw })
        }).then(res => res.json()).catch(() => null);

        if (!pwRes || !(pwRes.rsCode && pwRes.rsCode.startsWith("S-"))) {
          setMsg("passwordChangeMsg", (pwRes && pwRes.rsMsg) ? pwRes.rsMsg : "비밀번호 변경 중 오류가 발생했습니다.", "red");
          return false;
        }
      }

      alert("저장되었습니다.");
      window.location.href = "/usr/home/main";
      return false;
    }

    function cancelAll() {
      window.location.href = "/usr/home/main";
    }

    function pickRsMsg(json) {
      return json?.rsMsg ?? json?.msg ?? "";
    }
    function pickRsData(json) {
      return json?.rsData
        ?? json?.data
        ?? json?.resultData?.rsData
        ?? json?.resultData?.data
        ?? {};
    }
    function pickRestoreUntil(res) {
      const root = res || {};
      const rsData = pickRsData(root);
      const v =
        rsData.restoreUntil ??
        rsData.restoreUntilStr ??
        rsData.restoreDate ??
        rsData.restore_until ??
        "";
      const s = String(v ?? "").trim();
      return s.length > 0 ? s : "";
    }

    async function withdrawAccount() {
      const confirmWithdraw = confirm("정말 탈퇴하시겠습니까?\n7일 동안은 같은 계정으로 로그인하면 복구할 수 있습니다.");
      if (!confirmWithdraw) return;

      try {
        const res = await fetch("/usr/member/withdraw", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({})
        });

        const json = await res.json();
        console.log("withdraw response:", json);

        const rsMsg = pickRsMsg(json);
        let restoreUntil = pickRestoreUntil(json);

        // ✅ rsData에 날짜가 비면 rsMsg에서라도 날짜 뽑아내기(최후 보강)
        if (!restoreUntil) {
          const m = (rsMsg || "").match(/(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})/);
          if (m) restoreUntil = m[1];
        }

        // ✅ 알림 문구는 restoreUntil 있으면 그것으로 고정, 없으면 rsMsg 그대로
        let alertMsg;
        if (restoreUntil && restoreUntil.trim().length > 0) {
          alertMsg =
            `탈퇴가 완료되었습니다.\n` +
            `\${restoreUntil}까지 같은 계정으로 로그인하면 복구할 수 있습니다.\n` +
            `일주일이 지나면 계정 정보가 완전히 삭제됩니다.`;
        } else {
          alertMsg = rsMsg || "탈퇴 처리 결과를 확인할 수 없습니다.(날짜/메시지 없음)";
        }

        alert(alertMsg);

        const ok = (json.rsCode || json.resultCode || "").startsWith("S-");
        if (ok) window.location.href = "/logout";
      } catch (e) {
        console.error(e);
        alert("회원 탈퇴 요청 중 오류가 발생했습니다.");
      }
    }
  </script>
</head>

<body onload="updateNicknameNotice();">
  <div class="page-wrap">
    <div class="page-title">마이페이지</div>

    <div class="grid">
      <!-- 좌측: 계정 요약 -->
      <div class="card">
        <c:if test="${not empty member}">
          <h3>계정 정보</h3>
          <div class="kv"><div class="k">아이디</div><div class="v">${member.username}</div></div>
          <div class="kv"><div class="k">이름</div><div class="v">${member.realName}</div></div>
          <div class="kv"><div class="k">나이</div><div class="v">${member.age}</div></div>
          <div class="kv"><div class="k">일일 목표량</div><div class="v">${member.dailyTarget}</div></div>

          <div class="divider"></div>

          <div class="withdraw-wrap">
            <button type="button" class="btn-danger" onclick="withdrawAccount();">회원 탈퇴</button>
          </div>
        </c:if>
      </div>

      <!-- 우측: 프로필 수정 -->
      <div class="card">
        <h3>프로필 수정</h3>

        <form onsubmit="return saveProfile(event);">
          <div class="row">
            <p class="label">닉네임</p>
            <input type="hidden" id="nicknameOriginal" value="${member.nickname}" />

            <div class="nickname-line">
              <input type="text" id="nickname" name="nickname" value="${member.nickname}" />
              <button type="button" class="btn-outline" onclick="handleNicknameCheck();">중복 확인</button>
            </div>

            <p id="nicknameNotice" class="msg" style="color:#777;">닉네임은 변경 후 30일 동안 다시 변경할 수 없습니다.</p>
            <p id="nicknameStatus" class="msg"></p>
          </div>

          <div class="row">
            <p class="label">이메일</p>
            <input type="email" id="email" name="email" value="${member.email}" />
          </div>

          <div class="row">
            <p class="label">지역</p>
            <select id="region" name="region">
              <c:set var="regions" value="서울,경기,부산,대구,인천,광주,대전,울산,강원,경북,경남,충북,충남,전북,전남,제주,기타" />
              <c:forEach var="r" items="${regions.split(',')}">
                <option value="${r}" ${member.region==r ? "selected" : "" }>${r}</option>
              </c:forEach>
            </select>
          </div>

          <div class="row">
            <p class="label">일일 목표량</p>
            <select id="dailyTarget" name="dailyTarget">
              <c:forEach var="val" items="10,30,50,70,100">
                <option value="${val}" ${member.dailyTarget==val ? "selected" : "" }>${val}개</option>
              </c:forEach>
            </select>
          </div>

          <div class="divider"></div>

          <div class="row">
            <p class="label">새 비밀번호 (변경 시에만 입력)</p>
            <input type="password" id="newPassword" name="password" oninput="checkPasswordMatch();" />
          </div>

          <div class="row">
            <p class="label">비밀번호 확인</p>
            <input type="password" id="newPasswordConfirm" oninput="checkPasswordMatch();" />
            <p id="passwordChangeMsg" class="msg"></p>
          </div>

          <p id="profileMsg" class="msg"></p>

          <div class="actions-right">
            <button type="submit">저장하기</button>
            <button type="button" class="btn-outline" onclick="cancelAll();">취소하기</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</body>

</html>
