<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<header class="topbar">
  <div class="container topbar-inner">
    <a class="brand" href="${ctx}/usr/home/main">
      <span class="brand-badge"></span>
      <span>My Memory Book</span>
    </a>

    <nav class="nav">
      <a href="${ctx}/learning/wordbook">오늘의 단어장</a>
      <a href="${ctx}/learning/quiz">오늘의 퀴즈</a>
      <a href="${ctx}/usr/study/log">학습 로그</a>
      <a href="${ctx}/usr/article/list?boardId=2">Q&amp;A</a>
    </nav>

    <div class="userbox">
      <c:choose>
        <c:when test="${pageContext.request.userPrincipal == null}">
          <button class="btn btn-primary" onclick="location.href='${ctx}/usr/member/login'">로그인</button>
        </c:when>
        <c:otherwise>
          <c:set var="displayNickname"
                                value="${not empty member ? member.nickname : pageContext.request.userPrincipal.name}" />
                            <a href="/usr/member/myPage" style="margin-right:8px;">${displayNickname} 님</a>
          <form action="${ctx}/logout" method="post" style="display:inline;margin:0;">
            <button type="submit" class="btn btn-ghost">로그아웃</button>
          </form>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</header>

<main class="page">
  <div class="container">
