<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>오늘의 학습 단어 - My Memory Book</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .page-title { margin-bottom: 8px; }
        .sub-info { color: #555; margin-bottom: 16px; }
        .table { width: 100%; border-collapse: collapse; }
        .table th, .table td { border: 1px solid #e5e5e5; padding: 10px; text-align: left; vertical-align: top; }
        .table th { background: #f3f6ff; }
        .small { font-size: 13px; color: #666; }
        .secondary { background: #f1f1f1; color: #333; border: 1px solid #ccc; padding: 8px 12px; border-radius: 8px; cursor: pointer; }
        .primary { background: #2c6bed; color: #fff; border: none; padding: 10px 16px; border-radius: 8px; cursor: pointer; }
    </style>
</head>
<body>
    <h1 class="page-title">오늘의 학습 단어</h1>
    <p class="sub-info">
        오늘 목표 <strong>${dailyTarget}</strong>개 중 준비된 단어는 총 <strong>${todayWords.size()}</strong>개입니다.
    </p>

    <c:if test="${not empty wordbookError}">
        <div style="padding: 12px; border: 1px solid #f97316; background: #fff7ed; color: #9a3412; border-radius: 6px; margin-bottom: 12px;">
            ${wordbookError}
        </div>
    </c:if>

    <c:if test="${empty todayWords}">
        <p>오늘 학습할 단어가 없습니다. (목표를 설정해주세요!)</p>
        <button class="secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
    </c:if>

    <c:if test="${not empty todayWords}">
        <p class="small">각 단어의 뜻과 예문을 확인하고 발음을 들어보세요.</p>
        <div style="max-height: 500px; overflow: auto; margin-top: 10px;">
            <table class="table">
                <thead>
                    <tr>
                        <th style="width: 18%;">단어</th>
                        <th style="width: 22%;">뜻</th>
                        <th>예문 / 해석</th>
                        <th style="width: 90px;">발음</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="word" items="${todayWords}">
                        <c:set var="meaningVal" value="${(not empty word.meaning and word.meaning ne 'null') ? word.meaning : word.spelling}" />
                        <c:set var="exampleVal" value="${(not empty word.exampleSentence and word.exampleSentence ne 'null') ? word.exampleSentence : ''}" />
                        <tr>
                            <td>${fn:escapeXml(word.spelling)}</td>
                            <td>${fn:escapeXml(meaningVal)}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty exampleVal}">
                                        ${fn:escapeXml(exampleVal)}
                                    </c:when>
                                    <c:otherwise>
                                        예문이 없습니다.
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <button class="secondary" onclick="playAudio('${fn:escapeXml(word.spelling)}', '${word.audioPath != null ? fn:escapeXml(word.audioPath) : ""}')">듣기</button>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <div style="margin-top: 12px;">
            <button class="primary" onclick="location.href='/learning/quiz'">오늘의 퀴즈 풀기</button>
            <button class="secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
        </div>
    </c:if>

    <script>
        function playAudio(spelling, audioPath) {
            if (audioPath) {
                const audio = new Audio(audioPath);
                audio.play().catch(() => fallbackSpeak(spelling));
            } else {
                fallbackSpeak(spelling);
            }
        }

        function fallbackSpeak(text) {
            if (!window.speechSynthesis) {
                alert('브라우저에서 음성 출력이 지원되지 않습니다.');
                return;
            }
            const utter = new SpeechSynthesisUtterance(text);
            utter.lang = 'en-US';
            speechSynthesis.cancel();
            speechSynthesis.speak(utter);
        }
    </script>
</body>
</html>
