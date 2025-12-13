<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>학습 로그 - My Memory Book</title>
    <style>
        body { font-family: "Noto Sans KR", Arial, sans-serif; margin: 32px; }
        h1 { margin-bottom: 8px; }
        .desc { color: #666; margin-bottom: 18px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ececec; padding: 10px 12px; text-align: left; }
        th { background-color: #f7f8fb; }
        tbody tr:nth-child(even) { background-color: #fafafa; }
        .tag { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 12px; }
        .tag.quiz { background: #e0f0ff; color: #1d4ed8; }
        .tag.book { background: #e7f6ec; color: #047857; }
        .tag.other { background: #eee; color: #555; }
        .status { font-weight: bold; }
        .status.correct { color: #0f9d58; }
        .status.wrong { color: #d93025; }
        .actions { margin-top: 18px; }
        .btn { padding: 10px 18px; border-radius: 8px; border: none; cursor: pointer; font-size: 15px; }
        .btn.secondary { background: #f0f2f5; color: #333; border: 1px solid #d1d5db; }
    </style>
</head>
<body>
    <h1>학습 로그</h1>
    <p class="desc">최근 100개의 학습 기록입니다. (퀴즈/단어장 별로 기록됩니다.)</p>

    <c:if test="${empty records}">
        <p>아직 학습 기록이 없습니다. 오늘의 단어장을 학습하거나 퀴즈를 풀어보세요!</p>
    </c:if>

    <c:if test="${not empty records}">
        <table>
            <thead>
                <tr>
                    <th>일시</th>
                    <th>유형</th>
                    <th>단어</th>
                    <th>결과</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="record" items="${records}">
                    <tr>
                        <td>${record.studiedAtDisplay}</td>
                        <td>
                            <c:choose>
                                <c:when test="${record.studyType eq 'QUIZ'}">
                                    <span class="tag quiz">퀴즈</span>
                                </c:when>
                                <c:when test="${record.studyType eq 'BOOK'}">
                                    <span class="tag book">단어장</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="tag other">${record.studyType}</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${record.word ne null}">
                                    ${record.word.spelling}
                                </c:when>
                                <c:otherwise>
                                    -
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${record.correct}">
                                    <span class="status correct">정답</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status wrong">오답</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:if>

    <div class="actions">
        <button class="btn secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
    </div>
</body>
</html>
