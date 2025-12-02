<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>메인 - My Memory Book</title>
</head>
<body>

<h1 style="color:red;">★ 여기가 진짜 메인 JSP 입니다 ★</h1>
    
    <!-- 내 캐릭터 영역 -->
    <div style="border:1px solid black; padding:10px; width:220px; display:inline-block; vertical-align:top;">
        <h3>내 캐릭터</h3>
        <p>
            <!-- 나중에 레벨별로 이미지 바꿔 끼우면 됨 -->
            (3D 캐릭터 들어갈 자리)<br>
            <c:if test="${not empty member}">
                닉네임: ${member.nickname}<br>
                레벨: ${member.characterLevel}<br>
                경험치: ${member.currentExp} / 100
            </c:if>
        </p>
    </div>

    <!-- 랭킹 & 공지사항 -->
    <div style="display:inline-block; margin-left:20px; vertical-align:top; width:500px;">
        <!-- 랭킹 -->
        <div style="border:1px solid blue; padding:10px; margin-bottom:10px;">
            <h3>🏆 사용자 랭킹</h3>
            <p>(랭킹 데이터는 나중에 study_record 기반으로 채우기)</p>
            <%-- 예시: c:forEach로 ranking 돌리기 --%>
            <%-- 
            <c:forEach var="r" items="${ranking}" varStatus="st">
                ${st.index + 1}위 - ${r.nickname} (${r.totalPoint}점)<br>
            </c:forEach>
            --%>
        </div>

        <!-- 공지사항만 보여주기 (Q&A는 메인에 안 보이게) -->
        <div style="border:1px solid #aaa; padding:10px;">
            <h3>📢 공지사항</h3>
            <c:if test="${empty notices}">
                <p>등록된 공지사항이 없습니다.</p>
            </c:if>
            <c:if test="${not empty notices}">
                <ul>
                    <c:forEach var="article" items="${notices}">
                        <li>
                            <a href="/usr/article/detail?id=${article.id}">
                                ${article.title}
                            </a>
                        </li>
                    </c:forEach>
                </ul>
                <p style="text-align:right;">
                    <a href="/usr/article/list?boardId=1">공지사항 전체 보기 ▶</a>
                </p>
            </c:if>
        </div>
    </div>
    
    <hr style="margin-top:30px;">

    <!-- 학습 메뉴 영역 -->
    <h2>📚 학습 메뉴</h2>
    <div style="margin-top:10px;">
        <button style="width:200px; height:50px; font-size:18px;"
                onclick="location.href='/learning/today'">
            📖 오늘의 단어 공부
        </button>
        <button style="width:200px; height:50px; font-size:18px; margin-left:10px;"
                onclick="location.href='/usr/study/log'">
            📊 내 학습 로그
        </button>
        <!-- 🔹 여기에서만 Q&A로 이동 -->
        <button style="width:200px; height:50px; font-size:18px; margin-left:10px;"
                onclick="location.href='/usr/article/list?boardId=3'">
            ❓ 질문하기(Q&A 게시판)
        </button>
    </div>

</body>
</html>
