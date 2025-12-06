<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입</title>
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

    // 최종 검증
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
        return true;
    }
</script>
</head>
<body>
    <h1>회원 가입</h1>
    <form action="/usr/member/doJoin" method="post" onsubmit="return validateForm(this);">
        <input type="hidden" id="idChecked" value="N">
        <input type="hidden" id="nicknameChecked" value="N">
        
        <p>아이디 <input type="text" id="username" name="username" required>
           <button type="button" onclick="checkId()">중복확인</button></p>
        
        <p>비밀번호: <input type="password" id="password" name="password" required oninput="checkPasswordMatch()"></p>
        <p>비밀번호 확인: <input type="password" id="passwordConfirm" required oninput="checkPasswordMatch()"></p>
        <p id="passwordMsg" style="margin:4px 0 12px 0; font-size: 12px; color: red; height: 16px;"></p>
        
        <p>이름: <input type="text" name="name" required></p>

        <p>이메일: <input type="email" name="email" required></p>
        
        <p>닉네임: <input type="text" id="nickname" name="nickname" required onblur="checkNickname()" oninput="document.getElementById('nicknameChecked').value='N'; document.getElementById('nicknameMsg').textContent='';"></p>
        <p id="nicknameMsg" style="margin:4px 0 12px 0; font-size: 12px; color: red; height: 16px;"></p>
        
        <p>나이: <input type="number" name="age" min="1" max="100"></p>
        
        <p>거주지역 
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
        </p>
        
        <p>일일 학습 목표량: 
            <select name="dailyTarget">
                <option value="30">30개</option>
                <option value="50">50개</option>
                <option value="100">100개</option>
            </select>
        </p>
        
        <button type="submit">가입하기</button>
    </form>
</body>
</html>
