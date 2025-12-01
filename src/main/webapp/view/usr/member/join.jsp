<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입</title>
<script>
    // 아이디 중복 체크
    function checkId() {
        const username = document.getElementById("username").value;
        if(username.length < 4) { alert("아이디는 4자 이상이어야 합니다."); return; }
        
        fetch('/usr/member/checkUsername?username=' + username)
            .then(res => res.json())
            .then(data => {
                if(data.result === 'success') {
                    alert("사용 가능한 아이디입니다.");
                    document.getElementById("idChecked").value = "Y";
                } else {
                    alert("이미 사용중인 아이디입니다.");
                    document.getElementById("idChecked").value = "N";
                }
            });
    }

    // 폼 제출 전 검증
    function validateForm(form) {
        if(document.getElementById("idChecked").value !== "Y") {
            alert("아이디 중복 체크를 해주세요.");
            return false;
        }
        return true;
    }
</script>
</head>
<body>
    <h1>📝 회원가입</h1>
    <form action="/usr/member/doJoin" method="post" onsubmit="return validateForm(this);">
        <input type="hidden" id="idChecked" value="N">
        
        <p>아이디: <input type="text" id="username" name="username" required> 
           <button type="button" onclick="checkId()">중복확인</button></p>
        
        <p>비밀번호: <input type="password" name="password" required></p>
        
        <p>이름: <input type="text" name="name" required></p>
        
        <p>닉네임(유니크): <input type="text" name="nickname" required></p>
        
        <p>나이: <input type="number" name="age" min="1" max="100"></p>
        
        <p>거주지역: 
            <select name="region">
                <option value="서울">서울</option>
                <option value="경기">경기</option>
                <option value="인천">인천</option>
                <option value="대전">대전</option>
                <option value="대구">대구</option>
                <option value="부산">부산</option>
                <option value="광주">광주</option>
                <option value="울산">울산</option>
                <option value="강원">강원</option>
                <option value="제주">제주</option>
            </select>
        </p>
        
        <p>일일 학습 목표: 
            <select name="dailyTarget">
                <option value="30">30개</option>
                <option value="50">50개</option>
                <option value="100">100개</option>
            </select>
        </p>
        
        <button type="submit">가입완료</button>
    </form>
</body>
</html>