<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>마이페이지</title>
    <style>
        body {
            font-family: "Noto Sans KR", Arial, sans-serif;
        }
        .modal-backdrop {
            position: fixed;
            inset: 0;
            display: none;
            align-items: center;
            justify-content: center;
            background: rgba(0, 0, 0, 0.45);
            z-index: 1000;
        }
        .modal-console {
            width: 380px;
            padding: 16px;
            background: #0f1115;
            color: #e6e6e6;
            border: 1px solid #303540;
            box-shadow: 0 10px 28px rgba(0, 0, 0, 0.55);
            border-radius: 6px;
            font-family: Consolas, "Courier New", monospace;
        }
        .modal-console input[type="text"] {
            width: 100%;
            padding: 10px 12px;
            margin-top: 8px;
            background: #161a20;
            border: 1px solid #323847;
            color: #e6e6e6;
            border-radius: 4px;
        }
        .modal-actions {
            margin-top: 12px;
            text-align: right;
            display: flex;
            justify-content: flex-end;
            gap: 8px;
        }
    </style>
    <script>
        let nicknameAvailable = true;
        let nicknameChangeAllowed = ${nicknameChangeAllowed ? "true" : "false"};
        let nextNicknameChangeDateText = "${nextNicknameChangeDate}";
        let nicknameDaysLeft = ${nicknameDaysLeft != null ? nicknameDaysLeft : 0};

        function formatDateKR(dateObj) {
            const y = dateObj.getFullYear();
            const m = String(dateObj.getMonth() + 1).padStart(2, "0");
            const d = String(dateObj.getDate()).padStart(2, "0");
            const hh = String(dateObj.getHours()).padStart(2, "0");
            const mm = String(dateObj.getMinutes()).padStart(2, "0");
            return `${y}년 ${m}월 ${d}일 ${hh}시 ${mm}분`;
        }

        function parseDateFromRaw(raw) {
            if (!raw) return null;
            const nums = raw.match(/\d+/g);
            if (!nums || nums.length < 3) return null;
            const [y, mo, d, h = 0, mi = 0, s = 0] = nums.map(Number);
            return new Date(y, (mo || 1) - 1, d, h || 0, mi || 0, s || 0);
        }

        function computeNicknameWindow() {
            if (!nextNicknameChangeDateText) {
                const rawLast = document.getElementById("lastNicknameUpdatedAt")?.value || "";
                const lastDate = parseDateFromRaw(rawLast);
                if (lastDate) {
                    const nextDate = new Date(lastDate.getTime());
                    nextDate.setDate(nextDate.getDate() + 30);
                    nextNicknameChangeDateText = formatDateKR(nextDate);
                    const diffMs = nextDate - new Date();
                    const diffDays = Math.max(0, Math.floor(diffMs / (1000 * 60 * 60 * 24)));
                    nicknameDaysLeft = diffDays;
                    nicknameChangeAllowed = new Date() >= nextDate;
                }
            }

            const notice = document.getElementById("nicknameRule");
            if (notice) {
                if (nicknameChangeAllowed) {
                    notice.textContent = "닉네임은 변경 후 30일 동안 다시 변경할 수 없습니다.";
                } else if (nextNicknameChangeDateText) {
                    const daysInfo = nicknameDaysLeft > 0 ? (nicknameDaysLeft + "일 후") : "";
                    notice.textContent = "닉네임은 " + (daysInfo ? daysInfo + " " : "") + "변경할 수 있습니다.";
                } else {
                    notice.textContent = "닉네임 변경이 현재 제한되어 있습니다.";
                }
            }
        }

        function openNicknameModal() {
            const modal = document.getElementById("nicknameModal");
            const nicknameInput = document.getElementById("nicknameInputModal");
            nicknameInput.value = document.getElementById("nickname").value;
            document.getElementById("nicknameMsg").textContent = "";
            modal.style.display = "flex";
            nicknameInput.focus();
        }

        function closeNicknameModal() {
            const modal = document.getElementById("nicknameModal");
            const nicknameInput = document.getElementById("nicknameInputModal");
            nicknameInput.value = document.getElementById("nickname").value;
            document.getElementById("nicknameMsg").textContent = "";
            modal.style.display = "none";
            nicknameAvailable = true;
        }

        function handleNicknameChangeClick() {
            computeNicknameWindow();
            if (!nicknameChangeAllowed) {
                const daysInfo = nicknameDaysLeft > 0 ? nicknameDaysLeft + "일 후 " : "";
                const message = nextNicknameChangeDateText
                    ? "닉네임 변경은 " + nextNicknameChangeDateText + "에 가능합니다."
                    : "닉네임 변경이 현재 제한되어 있습니다.";
                alert(message);
                return;
            }
            openNicknameModal();
        }

        function clearNicknameMsg() {
            document.getElementById("nicknameMsg").textContent = "";
        }

        async function checkNicknameAvailability() {
            const nicknameInput = document.getElementById("nicknameInputModal");
            const nicknameMsg = document.getElementById("nicknameMsg");
            const nickname = nicknameInput.value.trim();

            if (nickname.length === 0) {
                nicknameMsg.textContent = "닉네임을 입력해주세요.";
                nicknameMsg.style.color = "red";
                nicknameAvailable = false;
                return false;
            }

            try {
                const data = await fetch('/usr/member/checkNickname?nickname=' + encodeURIComponent(nickname)).then(res => res.json());

                if (data.result === "fail") {
                    nicknameMsg.textContent = "이미 사용중인 닉네임입니다.";
                    nicknameMsg.style.color = "red";
                    nicknameAvailable = false;
                    return false;
                } else {
                    nicknameMsg.textContent = "사용 가능한 닉네임입니다.";
                    nicknameMsg.style.color = "#52c41a";
                    nicknameAvailable = true;
                    return true;
                }
            } catch (e) {
                nicknameMsg.textContent = "닉네임 확인 중 오류가 발생했습니다.";
                nicknameMsg.style.color = "red";
                nicknameAvailable = false;
                return false;
            }
        }

        async function applyNicknameChange() {
            if (!nicknameChangeAllowed) {
                const daysInfo = nicknameDaysLeft > 0 ? nicknameDaysLeft + "일 후 " : "";
                const message = nextNicknameChangeDateText
                    ? "닉네임 변경은 " + nextNicknameChangeDateText + "에 가능합니다."
                    : "닉네임 변경이 현재 제한되어 있습니다.";
                alert(message);
                return false;
            }
            const isAvailable = await checkNicknameAvailability();
            if (!isAvailable) {
                return false;
            }

            const nicknameInput = document.getElementById("nicknameInputModal");
            const nicknameValue = nicknameInput.value.trim();

            document.getElementById("nickname").value = nicknameValue;
            document.getElementById("nicknameDisplay").textContent = nicknameValue;
            document.getElementById("profileMsg").textContent = "";
            closeNicknameModal();
            return true;
        }

        function checkPasswordMatch() {
            const pw = document.getElementById("newPassword").value;
            const pwConfirm = document.getElementById("newPasswordConfirm").value;
            const msg = document.getElementById("passwordChangeMsg");

            if (pw.length === 0 && pwConfirm.length === 0) {
                msg.textContent = "";
                return true;
            }

            if (pw === pwConfirm) {
                msg.textContent = "";
                return true;
            } else {
                msg.textContent = "비밀번호가 일치하지 않습니다.";
                msg.style.color = "red";
                return false;
            }
        }

        async function saveAll() {
            const nickname = document.getElementById("nickname").value.trim();
            const emailInput = document.getElementById("email");
            const emailOriginal = document.getElementById("emailOriginal").value;
            const email = emailInput.value.trim();
            const region = document.getElementById("region").value.trim();
            const dailyTarget = document.getElementById("dailyTarget").value;
            const pw = document.getElementById("newPassword").value;
            const pwConfirm = document.getElementById("newPasswordConfirm").value;
            const profileMsg = document.getElementById("profileMsg");
            const passwordMsg = document.getElementById("passwordChangeMsg");

            if (nickname.length === 0) {
                profileMsg.textContent = "닉네임을 입력해주세요.";
                profileMsg.style.color = "red";
                return false;
            }

            const emailToSend = email.length === 0 ? emailOriginal : email;

            profileMsg.textContent = "";

            const profileRes = await fetch('/usr/member/modifyProfile', {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ nickname, email: emailToSend, region, dailyTarget })
            }).then(res => res.json()).catch(() => null);

            if (!profileRes || !(profileRes.rsCode && profileRes.rsCode.startsWith("S-"))) {
                profileMsg.textContent = (profileRes && profileRes.rsMsg) ? profileRes.rsMsg : "프로필 저장 중 오류가 발생했습니다.";
                profileMsg.style.color = "red";
                return false;
            }

            profileMsg.textContent = profileRes.rsMsg || "프로필이 저장되었습니다.";
            profileMsg.style.color = "green";

            if (pw.length > 0 || pwConfirm.length > 0) {
                if (!checkPasswordMatch()) {
                    return false;
                }
                const pwRes = await fetch('/usr/member/modifyPassword', {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({ password: pw })
                }).then(res => res.json()).catch(() => null);

                if (!pwRes || !(pwRes.rsCode && pwRes.rsCode.startsWith("S-"))) {
                    passwordMsg.textContent = (pwRes && pwRes.rsMsg) ? pwRes.rsMsg : "비밀번호 변경 중 오류가 발생했습니다.";
                    passwordMsg.style.color = "red";
                    return false;
                }
                passwordMsg.textContent = pwRes.rsMsg || "비밀번호가 변경되었습니다.";
                passwordMsg.style.color = "green";
            }

            // 저장 후 메인 페이지로 이동합니다.
            window.location.href = "/usr/home/main";
            return false;
        }

        function cancelAll() {
            window.location.href = "/usr/home/main";
        }

        async function withdrawAccount() {
            const ok = confirm("정말 탈퇴하시겠습니까? 7일간 데이터 보관 후 삭제됩니다.");
            if (!ok) return;
            const res = await fetch('/usr/member/withdraw', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            }).then(r => r.json()).catch(() => null);
            if (!res || !(res.rsCode && res.rsCode.startsWith("S-"))) {
                alert(res && res.rsMsg ? res.rsMsg : "탈퇴 처리 중 오류가 발생했습니다.");
                return;
            }
            alert(res.rsMsg + "\n보관 만료일: " + (res.data && res.data.restoreUntil ? res.data.restoreUntil : ""));
            window.location.href = "/logout";
        }

        window.addEventListener("DOMContentLoaded", computeNicknameWindow);
    </script>
</head>
<body>
    <h1>마이페이지</h1>

    <div style="width:440px; border:1px solid #ccc; padding:16px; margin-top:16px;">
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
            <form onsubmit="return saveAll();">
                <p>닉네임</p>
                <div style="display:flex; align-items:center; gap:8px;">
                    <span id="nicknameDisplay" style="flex:1; padding:8px; border:1px solid #ccc; border-radius:4px; background:#f7f7f7;">${member.nickname}</span>
                    <button type="button" onclick="handleNicknameChangeClick();">닉네임 변경하기</button>
                </div>
                <input type="hidden" id="nickname" name="nickname" value="${member.nickname}" />
                <input type="hidden" id="lastNicknameUpdatedAt" value="${member.nicknameUpdatedAt}" />
                <p id="nicknameRule" style="font-size:12px; color:#777; margin-top:6px;">닉네임은 변경 후 30일 동안 다시 변경할 수 없습니다.</p>

                <p style="margin-top:10px;">이메일</p>
                <input type="hidden" id="emailOriginal" value="${member.email}" />
                <input type="email" id="email" name="email" value="${member.email}" style="width:100%;" />

                <p style="margin-top:10px;">지역</p>
                <select id="region" name="region" style="width:100%;">
                    <option value="서울" ${member.region == '서울' ? "selected" : ""}>서울</option>
                    <option value="경기" ${member.region == '경기' ? "selected" : ""}>경기</option>
                    <option value="부산" ${member.region == '부산' ? "selected" : ""}>부산</option>
                    <option value="대구" ${member.region == '대구' ? "selected" : ""}>대구</option>
                    <option value="인천" ${member.region == '인천' ? "selected" : ""}>인천</option>
                    <option value="광주" ${member.region == '광주' ? "selected" : ""}>광주</option>
                    <option value="대전" ${member.region == '대전' ? "selected" : ""}>대전</option>
                    <option value="울산" ${member.region == '울산' ? "selected" : ""}>울산</option>
                    <option value="강원" ${member.region == '강원' ? "selected" : ""}>강원</option>
                    <option value="경북" ${member.region == '경북' ? "selected" : ""}>경북</option>
                    <option value="경남" ${member.region == '경남' ? "selected" : ""}>경남</option>
                    <option value="충북" ${member.region == '충북' ? "selected" : ""}>충북</option>
                    <option value="충남" ${member.region == '충남' ? "selected" : ""}>충남</option>
                    <option value="전북" ${member.region == '전북' ? "selected" : ""}>전북</option>
                    <option value="전남" ${member.region == '전남' ? "selected" : ""}>전남</option>
                    <option value="제주" ${member.region == '제주' ? "selected" : ""}>제주</option>
                    <option value="기타" ${member.region == '기타' ? "selected" : ""}>기타</option>
                </select>

                <p style="margin-top:10px;">일일 목표량</p>
                <select id="dailyTarget" name="dailyTarget" style="width:100%;">
                    <option value="10" ${member.dailyTarget == 10 ? "selected" : ""}>10개</option>
                    <option value="30" ${member.dailyTarget == 30 ? "selected" : ""}>30개</option>
                    <option value="50" ${member.dailyTarget == 50 ? "selected" : ""}>50개</option>
                    <option value="70" ${member.dailyTarget == 70 ? "selected" : ""}>70개</option>
                    <option value="100" ${member.dailyTarget == 100 ? "selected" : ""}>100개</option>
                </select>

                <p style="margin-top:16px;">새 비밀번호 (변경 시에만 입력)</p>
                <input type="password" id="newPassword" name="password" oninput="checkPasswordMatch();" style="width:100%;" />

                <p style="margin-top:10px;">비밀번호 확인</p>
                <input type="password" id="newPasswordConfirm" oninput="checkPasswordMatch();" style="width:100%;" />
                <p id="passwordChangeMsg" style="font-size:12px; margin-top:6px; height:16px;"></p>

                <p id="profileMsg" style="font-size:12px; margin-top:6px; height:16px;"></p>

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

    <div id="nicknameModal" class="modal-backdrop">
        <div class="modal-console">
            <div style="font-weight:bold; margin-bottom:4px;">닉네임 변경</div>
            <div style="font-size:12px; color:#9ecbff;">새 닉네임을 입력하고 중복 여부를 확인하세요.</div>
            <input type="text" id="nicknameInputModal" placeholder="새 닉네임 입력" oninput="clearNicknameMsg();" />
            <p id="nicknameMsg" style="font-size:12px; margin-top:6px; height:16px;"></p>
            <div class="modal-actions">
                <button type="button" onclick="checkNicknameAvailability();">중복 확인</button>
                <button type="button" onclick="applyNicknameChange();" style="background:#52c41a; color:#fff; border:1px solid #3a8c12;">적용</button>
                <button type="button" onclick="closeNicknameModal();">닫기</button>
            </div>
        </div>
    </div>
</body>
</html>
