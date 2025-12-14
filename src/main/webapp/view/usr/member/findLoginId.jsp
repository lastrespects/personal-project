<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>아이디 찾기</title>
        <script>
            let __findingLoginId = false;

            async function findLoginId() {
                if (__findingLoginId) return;

                const btn = document.getElementById("btnFindLoginId");
                const prevText = btn.innerText;

                try {
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

                    __findingLoginId = true;
                    btn.disabled = true;
                    btn.innerText = "전송 중...";

                    // URLSearchParams 사용 권장 (인코딩 자동 처리)
                    const res = await fetch('/usr/member/doFindLoginId', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: new URLSearchParams({ name, email }) // Controller param: name, email
                    });

                    const data = await res.json().catch(() => null);

                    if (!data) {
                        alert("처리 결과를 확인할 수 없습니다.");
                        return;
                    }

                    // 서버/프론트 키 혼용 대비
                    const rsMsg = data.rsMsg ?? data.msg ?? "";
                    alert(rsMsg || '처리 결과를 확인할 수 없습니다.');

                    if (data.rsCode && data.rsCode.startsWith('S-')) {
                        window.location.href = "/login";
                    }
                } catch (e) {
                    console.error(e);
                    alert('아이디 찾기 요청 중 오류가 발생했습니다.');
                } finally {
                    btn.disabled = false;
                    btn.innerText = prevText;
                    __findingLoginId = false;
                }
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
                <button type="button" id="btnFindLoginId" onclick="findLoginId()">아이디 찾기</button>
                <button onclick="history.back();" style="margin-left:8px;">뒤로가기</button>
            </div>
            <div style="margin-top:10px;">
                <a href="/usr/member/findLoginPw">비밀번호 찾기</a> |
                <a href="/usr/member/login">로그인</a>
            </div>
        </div>
    </body>

    </html>