<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

        <!DOCTYPE html>
        <html>

        <head>
            <meta charset="UTF-8">
            <title>오늘의 단어 공부 - My Memory Book</title>
            <style>
                .word-card {
                    border: 1px solid #ddd;
                    padding: 15px;
                    margin-bottom: 10px;
                    border-radius: 8px;
                    background-color: #f9f9f9;
                }

                .word-spelling {
                    font-size: 24px;
                    font-weight: bold;
                    color: #333;
                }

                .word-meaning {
                    font-size: 18px;
                    color: #555;
                    margin-top: 5px;
                }

                .word-example {
                    font-style: italic;
                    color: #777;
                    margin-top: 5px;
                }

                .review-badge {
                    background-color: #ffcc00;
                    color: #fff;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-size: 12px;
                    vertical-align: middle;
                }
            </style>
        </head>

        <body>

            <h1>📖 오늘의 단어 공부</h1>

            <p>오늘 학습할 단어는 총 <strong>${todayWords.size()}</strong>개 입니다.</p>

            <div id="word-list">
                <c:if test="${empty todayWords}">
                    <p>오늘 학습할 단어가 없습니다. (목표 달성! 🎉)</p>
                </c:if>

                <c:forEach var="word" items="${todayWords}" varStatus="status">
                    <div class="word-card">
                        <div class="word-spelling">
                            ${status.count}. ${word.spelling}
                            <c:if test="${word.review}">
                                <span class="review-badge">복습</span>
                            </c:if>
                        </div>
                        <div class="word-meaning">${word.meaning}</div>
                        <c:if test="${not empty word.exampleSentence}">
                            <div class="word-example">Example: ${word.exampleSentence}</div>
                        </c:if>
                        <div style="margin-top:10px;">
                            <!-- 추후 오디오 재생 및 정답 확인 기능 추가 예정 -->
                            <button onclick="alert('발음 듣기 기능 준비 중')">🔊 듣기</button>
                            <button onclick="alert('정답 확인 기능 준비 중')">👁 정답 확인</button>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div style="margin-top: 20px;">
                <button onclick="location.href='/usr/home/main'">🏠 메인으로 돌아가기</button>
            </div>

        </body>

        </html>