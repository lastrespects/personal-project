<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <title>메인 - My Memory Book</title>
</head>

<body>

    <div style="text-align:right; margin:10px 0;">
        <c:choose>
            <c:when test="${pageContext.request.userPrincipal == null}">
                <button onclick="location.href='/login'">로그인</button>
            </c:when>
            <c:otherwise>
                <c:set var="displayNickname" value="${not empty member ? member.nickname : pageContext.request.userPrincipal.name}" />
                <a href="/usr/member/myPage" style="margin-right:8px;">${displayNickname}님</a>
                <form action="/logout" method="post" style="display:inline;">
                    <button type="submit">로그아웃</button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

    <h1 style="color:red;">(샘플) 메인 JSP 페이지</h1>

    <!-- 캐릭터 영역 -->
    <div style="border:1px solid black; padding:10px; width:220px; display:inline-block; vertical-align:top;">
        <h3>내 캐릭터</h3>
        <p>
            (3D 캐릭터 자리)<br>
            <c:if test="${not empty member}">
                닉네임: ${member.nickname}<br>
                레벨: ${member.characterLevel}<br>
                경험치: ${member.currentExp} / 100<br>
                일일 학습량: ${member.dailyTarget}
            </c:if>
        </p>
    </div>

    <!-- 학습 & 공지 -->
    <div style="display:inline-block; margin-left:20px; vertical-align:top; width:500px;">
        <!-- 학습 -->
        <div style="border:1px solid blue; padding:10px; margin-bottom:10px;">
            <h3>오늘 학습 메뉴</h3>
            <p>(학습 데이터는 추후 study_record 기반으로 채울 예정)</p>
        </div>

        <!-- 공지 -->
        <div style="border:1px solid #aaa; padding:10px;">
            <h3>최근 공지사항</h3>
            <c:if test="${empty notices}">
                <p>등록된 공지가 없습니다.</p>
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
                    <a href="/usr/article/list?boardId=1">공지 전체 보기 »</a>
                </p>
            </c:if>
        </div>
    </div>

    <hr style="margin-top:30px;">

    <!-- 학습 메뉴 영역 -->
    <h2>오늘 학습 메뉴</h2>
    <div style="margin-top:10px;">
        <button style="width:200px; height:50px; font-size:18px;" onclick="location.href='/learning/wordbook'">
            오늘의 단어장
        </button>
        <button style="width:200px; height:50px; font-size:18px; margin-left:10px;"
            onclick="location.href='/learning/quiz'">
            오늘의 퀴즈
        </button>
        <button style="width:200px; height:50px; font-size:18px; margin-left:10px;"
            onclick="location.href='/usr/study/log'">
            학습 로그
        </button>
        <button style="width:200px; height:50px; font-size:18px; margin-left:10px;"
            onclick="location.href='/usr/article/list?boardId=2'">
            질문하기(Q&A)
        </button>
    </div>

</body>

</html>
