<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <title>메인 - My Memory Book</title>
</head>

<body>
<%@ include file="/view/usr/common/header.jsp" %>

<main class="page">
  <div class="container">

    <!-- ✅ 공지사항: 메인 최상단(기존 “메인 카드 자리”) -->
    <section class="card" style="padding:18px; margin-bottom:16px;">
      <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
        <span class="badge primary">공지</span>

        <div style="flex:1; min-width:240px;">
          <c:choose>
            <c:when test="${not empty notices}">
              <ul style="margin:0; padding-left:18px;">
                <c:forEach var="a" items="${notices}">
                  <li style="margin:4px 0;">
                    <a href="${pageContext.request.contextPath}/usr/article/detail?id=${a.id}"
                       style="font-weight:800;">
                      ${a.title}
                    </a>
                  </li>
                </c:forEach>
              </ul>
            </c:when>
            <c:otherwise>
              <div class="subtitle">등록된 공지가 없습니다.</div>
            </c:otherwise>
          </c:choose>
        </div>

        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/usr/article/list?boardId=1"
           style="margin-left:auto;">
          전체 보기 →
        </a>
      </div>
    </section>

    <!-- ✅ 2열 레이아웃 -->
    <div style="display:grid; grid-template-columns: 280px 1fr; gap:16px; align-items:start;">
      <!-- 왼쪽: 내 캐릭터 -->
      <section class="card" style="padding:18px;">
        <h3 style="margin:0 0 12px; font-size:18px; letter-spacing:-.3px;">내 캐릭터</h3>

        <!-- ✅ 캐릭터 이미지 추가 -->
  <div style="display:flex; justify-content:center; margin: 6px 0 14px;">
    <img class="mmb-character"
         src="${pageContext.request.contextPath}/img/character.png"
         alt="MMB 캐릭터">
  </div>

        <c:choose>
          <c:when test="${not empty member}">
            <div style="display:grid; gap:10px;">
              <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:8px;">
                <span class="subtitle">닉네임</span>
                <b>${member.nickname}</b>
              </div>
              <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:8px;">
                <span class="subtitle">레벨</span>
                <b>${member.characterLevel}</b>
              </div>
              <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:8px;">
                <span class="subtitle">경험치</span>
                <b>${member.currentExp} / 100</b>
              </div>
              <div style="display:flex; justify-content:space-between;">
                <span class="subtitle">일일 학습량</span>
                <b>${member.dailyTarget}</b>
              </div>

              <a class="btn" href="${pageContext.request.contextPath}/usr/member/myPage" style="margin-top:10px; width:fit-content;">
                마이페이지
              </a>
            </div>
          </c:when>

          <c:otherwise>
            <p class="subtitle" style="margin:0;">로그인하면 캐릭터 정보가 표시돼요.</p>
          </c:otherwise>
        </c:choose>
      </section>

      <!-- 오른쪽: 요약 + Q&A -->
      <div style="display:grid; gap:16px;">

        <!-- 오늘 학습 요약 -->
        <section class="card" style="padding:18px;">
          <div style="display:flex; justify-content:space-between; align-items:center; gap:10px;">
            <h3 style="margin:0; font-size:18px; letter-spacing:-.3px;">오늘 학습 요약</h3>
            <span class="badge primary">Today</span>
          </div>

          <div style="margin-top:12px; display:grid; gap:10px;">
            <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:10px;">
              <span>오늘 목표</span>
              <b>${todayTarget}개</b>
            </div>
            <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:10px;">
              <span>푼 문제</span>
              <b>${quizSolvedCount}개</b>
            </div>
            <div style="display:flex; justify-content:space-between; border-bottom:1px dashed var(--line); padding-bottom:10px;">
              <span>남은 문제</span>
              <b>${quizRemainingCount}개</b>
            </div>
            <div style="display:flex; justify-content:space-between;">
              <span>오늘 학습한 단어</span>
              <b>${todayLearnedCount}개</b>
            </div>
          </div>

          <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top:14px;">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/learning/quiz">오늘의 퀴즈</a>
            <a class="btn" href="${pageContext.request.contextPath}/learning/wordbook">오늘의 단어장</a>
            <a class="btn" href="${pageContext.request.contextPath}/usr/study/log">학습 로그</a>
          </div>
        </section>

        <!-- ✅ 여기가 원래 공지 자리였던 곳 → Q&A 최신글 배치 -->
        <section class="card" style="padding:18px;">
          <div style="display:flex; justify-content:space-between; align-items:center; gap:10px;">
            <h3 style="margin:0; font-size:18px; letter-spacing:-.3px;">최근 Q&amp;A</h3>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/usr/article/list?boardId=2">전체 보기 →</a>
          </div>

          <c:choose>
            <c:when test="${empty qnas}">
              <p class="subtitle" style="margin:10px 0 0;">등록된 질문이 없습니다.</p>
            </c:when>
            <c:otherwise>
              <ul style="margin:12px 0 0; padding-left:18px;">
                <c:forEach var="q" items="${qnas}">
                  <li style="margin:6px 0;">
                    <a href="${pageContext.request.contextPath}/usr/article/detail?id=${q.id}" style="font-weight:800;">
                      ${q.title}
                    </a>
                  </li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>

      </div>
    </div>
  </div>
</main>

<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
