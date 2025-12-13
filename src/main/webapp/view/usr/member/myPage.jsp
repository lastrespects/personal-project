<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>마이페이지</title>
    <style>
        body { font-family: "Noto Sans KR", Arial, sans-serif; }
        .card { width: 460px; border: 1px solid #ccc; padding: 16px; margin-top: 16px; }
        .row { margin-top: 10px; }
        .label { margin: 4px 0; font-weight: bold; }
        .msg { font-size: 12px; min-height: 16px; }
        button { cursor: pointer; }
    </style>
    <script>
        const nicknameChangeAllowed = ${nicknameChangeAllowed ? "true" : "false"};
        const nextNicknameChangeDate = "${nextNicknameChangeDate}";
        const nicknameDaysLeft = ${nicknameDaysLeft != null ? nicknameDaysLeft : 0};

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

        async function withdrawAccount() {
            const confirmWithdraw = confirm("정말 탈퇴하시겠습니까?\n7일 동안은 같은 계정으로 로그인하면 복구할 수 있습니다.");
            if (!confirmWithdraw) return;

            try {
                const res = await fetch("/usr/member/withdraw", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" }
                }).then(r => r.json());

                console.log("withdraw response:", res);

                const ok = (res && (res.rsCode || res.resultCode || "")).startsWith("S-");
                if (!ok) {
                    alert(res?.rsMsg || res?.msg || "회원 탈퇴 처리 중 오류가 발생했습니다.");
                    return;
                }

                const restoreUntil = pickRestoreUntil(res);
                const restoreInfo = restoreUntil.length > 0 ? restoreUntil : "7일 이내";

                alert(`탈퇴가 완료되었습니다.\n${restoreInfo}까지 같은 계정으로 로그인하면 복구할 수 있습니다.\n일주일이 지나면 계정 정보가 완전히 삭제됩니다.`);
                window.location.href = "/logout";
            } catch (e) {
                alert("회원 탈퇴 요청 중 오류가 발생했습니다.");
            }
        }
    </script>
</head>
<body onload="updateNicknameNotice();">
    <h1>마이페이지</h1>

    <div class="card">
        <c:if test="${not empty member}">
            <section>
                <h3>계정 정보</h3>
                <p>아이디: <strong>${member.username}</strong></p>
                <p>이름: <strong>${member.realName}</strong></p>
                <p>나이: <strong>${member.age}</strong></p>
                <p>일일 목표량: <strong>${member.dailyTarget}</strong></p>
            </section>
        </c:if>

        <section style="margin-top:20px;">
            <h3>프로필 수정</h3>
            <form onsubmit="return saveProfile(event);">
                <div class="row">
                    <p class="label">닉네임</p>
                    <input type="hidden" id="nicknameOriginal" value="${member.nickname}" />
                    <div style="display:flex; gap:8px;">
                        <input type="text" id="nickname" name="nickname" value="${member.nickname}" style="flex:1;" />
                        <button type="button" onclick="handleNicknameCheck();">중복 확인</button>
                    </div>
                    <p id="nicknameNotice" class="msg" style="color:#777;">닉네임은 변경 후 30일 동안 다시 변경할 수 없습니다.</p>
                    <p id="nicknameStatus" class="msg"></p>
                </div>

                <div class="row">
                    <p class="label">이메일</p>
                    <input type="email" id="email" name="email" value="${member.email}" style="width:100%;" />
                </div>

                <div class="row">
                    <p class="label">지역</p>
                    <select id="region" name="region" style="width:100%;">
                        <c:set var="regions" value="서울,경기,부산,대구,인천,광주,대전,울산,강원,경북,경남,충북,충남,전북,전남,제주,기타"/>
                        <c:forEach var="r" items="${regions.split(',')}">
                            <option value="${r}" ${member.region == r ? "selected" : ""}>${r}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="row">
                    <p class="label">일일 목표량</p>
                    <select id="dailyTarget" name="dailyTarget" style="width:100%;">
                        <c:forEach var="val" items="10,30,50,70,100">
                            <option value="${val}" ${member.dailyTarget == val ? "selected" : ""}>${val}개</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="row">
                    <p class="label">새 비밀번호 (변경 시에만 입력)</p>
                    <input type="password" id="newPassword" name="password" oninput="checkPasswordMatch();" style="width:100%;" />
                </div>
                <div class="row">
                    <p class="label">비밀번호 확인</p>
                    <input type="password" id="newPasswordConfirm" oninput="checkPasswordMatch();" style="width:100%;" />
                    <p id="passwordChangeMsg" class="msg"></p>
                </div>

                <p id="profileMsg" class="msg"></p>

                <div style="margin-top:12px; text-align:right;">
                    <button type="submit">저장하기</button>
                    <button type="button" onclick="cancelAll();" style="margin-left:8px;">취소하기</button>
                </div>
            </form>

            <div style="margin-top:16px; text-align:right;">
                <button type="button" style="color:#c00;" onclick="withdrawAccount();">회원 탈퇴</button>
            </div>
        </section>
    </div>
</body>
</html>
