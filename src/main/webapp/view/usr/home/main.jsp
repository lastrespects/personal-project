<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>메인 - My Memory Book</title>
                <style>
                    body {
                        font-family: "Noto Sans KR", Arial, sans-serif;
                        margin: 24px;
                    }

                    h1 {
                        color: #d60000;
                    }

                    .panel {
                        border: 1px solid #d1d5db;
                        padding: 12px;
                        border-radius: 8px;
                    }

                    .stats p {
                        margin: 4px 0;
                    }

                    .menu-buttons button {
                        width: 200px;
                        height: 50px;
                        font-size: 18px;
                        margin-right: 10px;
                    }

                    .notice-list {
                        list-style: none;
                        padding-left: 16px;
                    }

                    .notice-list li {
                        margin-bottom: 6px;
                    }
                </style>
                <script>
                    // ✅ Flash Attribute 또는 URL 파라미터 msg 처리
                    window.addEventListener('load', () => {
                        let msg = '';

                        // 1. Flash Attribute (서버에서 전달된 값)
                        <c:if test="${not empty msg}">
                            msg = "${fn:escapeXml(msg)}";
                        </c:if>

                        // 2. URL Parameter (하위 호환성)
                        if (!msg) {
                            try {
                                const url = new URL(window.location.href);
                                const paramMsg = url.searchParams.get('msg');
                                if (paramMsg && paramMsg.trim().length > 0) {
                                    msg = paramMsg;
                                    // URL 정리
                                    url.searchParams.delete('msg');
                                    const qs = url.searchParams.toString();
                                    history.replaceState(null, '', url.pathname + (qs ? '?' + qs : ''));
                                }
                            } catch (e) { /* ignore */ }
                        }

                        if (msg && msg.trim().length > 0) {
                            alert(msg);
                        }
                    });
                </script>
            </head>

            <body>

                <div style="text-align:right; margin:10px 0;">
                    <c:choose>
                        <c:when test="${pageContext.request.userPrincipal == null}">
                            <button onclick="location.href='/login'">로그인</button>
                        </c:when>
                        <c:otherwise>
                            <c:set var="displayNickname"
                                value="${not empty member ? member.nickname : pageContext.request.userPrincipal.name}" />
                            <a href="/usr/member/myPage" style="margin-right:8px;">${displayNickname} 님</a>
                            <form action="/logout" method="post" style="display:inline;">
                                <button type="submit">로그아웃</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>

                <h1>(샘플) 메인 JSP 페이지</h1>

                <div style="display:flex; gap:20px; align-items:flex-start;">
                    <div class="panel" style="width:240px;">
                        <h3>내 캐릭터</h3>
                        <c:if test="${not empty member}">
                            <p>닉네임: ${member.nickname}</p>
                            <p>레벨: ${member.characterLevel}</p>
                            <p>경험치: ${member.currentExp} / 100</p>
                            <p>일일 학습량: ${member.dailyTarget}</p>
                        </c:if>
                    </div>

                    <div style="flex:1;">
                        <div class="panel stats" style="margin-bottom:12px; border-color:#3b82f6;">
                            <h3>오늘 학습 요약</h3>
                            <p>오늘 목표: <strong>${todayTarget}</strong>개</p>
                            <p>푼 문제: <strong>${quizSolvedCount}</strong>개</p>
                            <p>남은 문제: <strong>${quizRemainingCount}</strong>개</p>
                            <p>오늘 학습한 단어: <strong>${todayLearnedCount}</strong>개</p>
                        </div>

                        <div class="panel">
                            <h3>최근 공지사항</h3>
                            <c:if test="${empty notices}">
                                <p>등록된 공지가 없습니다.</p>
                            </c:if>
                            <c:if test="${not empty notices}">
                                <ul class="notice-list">
                                    <c:forEach var="article" items="${notices}">
                                        <li>
                                            <a href="/usr/article/detail?id=${article.id}">${article.title}</a>
                                        </li>
                                    </c:forEach>
                                </ul>
                                <p style="text-align:right;">
                                    <a href="/usr/article/list?boardId=1">공지 전체 보기 »</a>
                                </p>
                            </c:if>
                        </div>
                    </div>
                </div>

                <hr style="margin:30px 0;">

                <section>
                    <h2>오늘 학습 메뉴</h2>
                    <div class="menu-buttons" style="margin-top:10px;">
                        <button onclick="location.href='/learning/wordbook'">오늘의 단어장</button>
                        <button onclick="location.href='/learning/quiz'">오늘의 퀴즈</button>
                        <button onclick="location.href='/usr/study/log'">학습 로그</button>
                        <button onclick="location.href='/usr/article/list?boardId=2'">질문하기(Q&A)</button>
                    </div>
                </section>

            </body>

            </html>