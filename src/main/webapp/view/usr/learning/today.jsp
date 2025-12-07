<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ page import="java.util.List" %>
		<%@ page import="com.mmb.dto.TodayWordDto" %>
			<% // Controller에서 이미 json을 만들어 보냈으므로, 여기서는 아무 논리도 필요 없음. // 단지 wordCount를 알기 위해 리스트 사이즈만 체크 (Scriptlet 최소화)
				List<TodayWordDto> words = (List<TodayWordDto>)request.getAttribute("todayWords");
					int count = (words != null) ? words.size() : 0;

					// Controller에서 보낸 JSON string
					String json = (String) request.getAttribute("wordsJson");
					if (json == null) json = "[]";
					%>
					<!DOCTYPE html>
					<html>

					<head>
						<meta charset="UTF-8">
						<title>오늘의 퀴즈 - My Memory Book</title>
						<style>
							body {
								font-family: Arial, sans-serif;
								margin: 24px;
							}

							.page-title {
								margin-bottom: 8px;
							}

							.sub-info {
								color: #555;
								margin-bottom: 16px;
							}

							.quiz-card {
								border: 1px solid #ddd;
								border-radius: 10px;
								padding: 18px;
								max-width: 720px;
								background: #fafafa;
							}

							.progress {
								font-size: 14px;
								color: #666;
								margin-bottom: 8px;
							}

							.question {
								font-size: 20px;
								font-weight: bold;
								margin-bottom: 12px;
							}

							.option-list {
								display: grid;
								gap: 10px;
								margin-bottom: 10px;
							}

							.option-btn {
								padding: 12px 14px;
								border: 1px solid #ccc;
								border-radius: 8px;
								background: #fff;
								cursor: pointer;
								text-align: left;
								font-size: 15px;
								transition: all 0.15s ease;
							}

							.option-btn:hover {
								border-color: #7aa3ff;
								box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
							}

							.option-btn.correct {
								background: #e8f6ec;
								border-color: #4caf50;
								color: #256029;
							}

							.option-btn.wrong {
								background: #ffecec;
								border-color: #f44336;
								color: #b71c1c;
							}

							.feedback {
								min-height: 22px;
								margin-top: 6px;
								font-weight: bold;
							}

							.controls {
								margin-top: 12px;
								display: flex;
								gap: 10px;
							}

							.primary {
								background: #2c6bed;
								color: #fff;
								border: none;
								padding: 10px 16px;
								border-radius: 8px;
								cursor: pointer;
							}

							.secondary {
								background: #f1f1f1;
								color: #333;
								border: 1px solid #ccc;
								padding: 10px 16px;
								border-radius: 8px;
								cursor: pointer;
							}

							.meta {
								color: #777;
								font-size: 13px;
								margin-top: 4px;
							}

							.notice {
								margin-top: 8px;
								color: #c62828;
								font-weight: bold;
							}
						</style>
					</head>

					<body>
						<h1 class="page-title">오늘의 퀴즈</h1>
						<p class="sub-info">오늘 학습할 단어는 총 <strong>
								<%= count %>
							</strong>개입니다.</p>

						<% if (count==0) { %>
							<p>오늘 학습할 단어가 없습니다. (목표를 설정해주세요!)</p>
							<button class="secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
							<% } %>

								<div class="quiz-card">
									<% if (count> 0) { %>
										<div class="progress" id="progress">로딩 중...</div>
										<div class="question" id="question"></div>
										<div class="option-list" id="options"></div>
										<div class="feedback" id="feedback"></div>
										<div class="meta" id="example"></div>
										<div class="controls">
											<button class="primary" id="nextBtn" onclick="nextQuestion()" disabled>다음
												문제</button>
											<button class="secondary" onclick="location.href='/usr/home/main'">메인으로
												돌아가기</button>
										</div>
										<% } else { %>
											<div class="progress" id="progress"></div>
											<div class="question" id="question">오늘 학습할 단어가 없습니다.</div>
											<div class="option-list" id="options"></div>
											<div class="controls">
												<button class="secondary" onclick="location.href='/usr/home/main'">메인으로
													돌아가기</button>
											</div>
											<% } %>
								</div>

								<div class="notice" id="notice" style="display:none"></div>

								<script>
									// Server-side constructed JSON inserted directly
									// json 변수는 이미 Controller에서 이스케이프가 완료된 문자열
									const wordsData = <%= json %>;

									const progressEl = document.getElementById('progress');
									const questionEl = document.getElementById('question');
									const optionsEl = document.getElementById('options');
									const feedbackEl = document.getElementById('feedback');
									const exampleEl = document.getElementById('example');
									const nextBtn = document.getElementById('nextBtn');
									const noticeEl = document.getElementById('notice');

									let currentIndex = 0;
									let answered = false;

									function shuffle(arr) {
										for (let i = arr.length - 1; i > 0; i--) {
											const j = Math.floor(Math.random() * (i + 1));
											[arr[i], arr[j]] = [arr[j], arr[i]];
										}
										return arr;
									}

									function buildOptions(correctWord) {
										const others = wordsData.filter(w => w.spelling !== correctWord.spelling);
										shuffle(others);
										const candidates = others.slice(0, 3).map(w => w.spelling);
										candidates.push(correctWord.spelling);
										return shuffle(candidates);
									}

									function decodeHtmlEntities(str) {
										if (!str) return '';
										const txt = document.createElement('textarea');
										txt.innerHTML = str;
										return txt.value;
									}

									function renderQuestion() {
										try {
											if (!wordsData || !wordsData.length) return;
											const word = wordsData[currentIndex];
											if (!word) return;
											answered = false;
											nextBtn.disabled = true;
											feedbackEl.textContent = '';
											feedbackEl.style.color = '';
											exampleEl.textContent = '';

											let meaningText = word.meaning;
											if (!meaningText || meaningText === 'null' || meaningText.trim() === '') {
												meaningText = '(뜻 없음)';
											}

											progressEl.textContent = `문제 ${currentIndex + 1} / ${wordsData.length}`;
											questionEl.textContent = `뜻이 "${meaningText}"인 단어를 고르세요.`;

											const optionLabels = buildOptions(word);
											optionsEl.innerHTML = '';
											optionLabels.forEach(label => {
												const btn = document.createElement('button');
												btn.className = 'option-btn';
												btn.textContent = label;
												btn.onclick = () => selectAnswer(btn, label, word);
												optionsEl.appendChild(btn);
											});
										} catch (e) {
											console.error('renderQuestion error', e);
											if (noticeEl) {
												noticeEl.style.display = 'block';
												noticeEl.textContent = '오류 발생: ' + e.message;
											}
										}
									}

									function selectAnswer(button, chosen, word) {
										if (answered) return;
										answered = true;
										const buttons = optionsEl.querySelectorAll('.option-btn');
										buttons.forEach(btn => {
											btn.disabled = true;
											if (btn.textContent === word.spelling) {
												btn.classList.add('correct');
											}
										});
										if (chosen === word.spelling) {
											feedbackEl.textContent = '정답입니다!';
											feedbackEl.style.color = '#2e7d32';
										} else {
											feedbackEl.textContent = `아쉽네요. 정답은 "${word.spelling}" 입니다.`;
											feedbackEl.style.color = '#c62828';
											button.classList.add('wrong');
										}

										const exampleText = decodeHtmlEntities((word.example || '').trim());
										exampleEl.textContent = exampleText ? `예문: ${exampleText}` : '예문이 없습니다.';
										nextBtn.disabled = currentIndex >= wordsData.length - 1;
									}

									function nextQuestion() {
										if (currentIndex < wordsData.length - 1) {
											currentIndex += 1;
											renderQuestion();
										}
									}

									if (wordsData && wordsData.length) {
										renderQuestion();
									}
								</script>
								<div style="text-align:center; color:#ccc; font-size:12px; margin-top:20px;">v1.3
									(Controller Logic Refactor)</div>
					</body>

					</html>