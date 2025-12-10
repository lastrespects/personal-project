<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
                    max-width: 760px;
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
                    margin-bottom: 6px;
                }

                /* ✅ 문제 문장 바로 아래 메타 */
                .meta {
                    color: #777;
                    font-size: 13px;
                    margin-top: 2px;
                    margin-bottom: 8px;
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

                .primary:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                }

                .secondary {
                    background: #f1f1f1;
                    color: #333;
                    border: 1px solid #ccc;
                    padding: 10px 16px;
                    border-radius: 8px;
                    cursor: pointer;
                }

                /* INPUT */
                .input-area {
                    display: flex;
                    gap: 8px;
                    align-items: center;
                    margin-bottom: 10px;
                }

                .answer-input {
                    flex: 1;
                    padding: 10px 12px;
                    border: 1px solid #ccc;
                    border-radius: 8px;
                    font-size: 15px;
                    background: #fff;
                }

                .submit-btn {
                    padding: 10px 14px;
                    border-radius: 8px;
                    border: 1px solid #2c6bed;
                    background: #2c6bed;
                    color: white;
                    cursor: pointer;
                }

                .submit-btn:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                }

                /* MATCH */
                .match-wrap {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 12px;
                    margin-bottom: 10px;
                }

                .match-col {
                    background: #fff;
                    border: 1px solid #ddd;
                    border-radius: 10px;
                    padding: 10px;
                }

                .match-item {
                    padding: 10px 12px;
                    border: 1px solid #ccc;
                    border-radius: 8px;
                    margin-bottom: 8px;
                    cursor: pointer;
                    background: #fff;
                    transition: all 0.12s ease;
                    font-size: 14.5px;
                }

                .match-item:last-child {
                    margin-bottom: 0;
                }

                .match-item:hover {
                    border-color: #7aa3ff;
                }

                .match-item.selected {
                    border-color: #2c6bed;
                    background: #eef4ff;
                }

                .match-item.matched {
                    border-color: #4caf50;
                    background: #e8f6ec;
                    cursor: default;
                }

                .match-item.wrongflash {
                    border-color: #f44336;
                    background: #ffecec;
                }

                /* LISTEN */
                .listen-bar {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 8px;
                }

                .listen-btn {
                    padding: 8px 12px;
                    border-radius: 8px;
                    border: 1px solid #2c6bed;
                    background: #fff;
                    cursor: pointer;
                    font-size: 13px;
                }
            </style>
        </head>

        <body>

            <h1 class="page-title">오늘의 퀴즈</h1>
            <p class="sub-info">오늘 학습할 단어는 총 <strong>${todayWords.size()}</strong>개입니다.</p>

            <c:if test="${empty todayWords}">
                <p>오늘 학습할 단어가 없습니다. (목표를 설정해주세요!)</p>
                <button class="secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
            </c:if>

            <c:if test="${not empty todayWords}">
                <script type="application/json" id="wordsJsonData">
            <c:out value="${wordsJson}" escapeXml="false"/>
        </script>

                <div class="quiz-card">
                    <div class="progress" id="progress"></div>
                    <div class="question" id="question"></div>

                    <!-- ✅ 여기! 문제 문장 바로 아래에 뜻/예문 -->
                    <div class="meta" id="meaningMeta"></div>
                    <div class="meta" id="exampleMeta"></div>

                    <div class="listen-bar" id="listenBar" style="display:none;">
                        <button class="listen-btn" id="listenBtn">🔊 발음 재생</button>
                        <span class="meta" id="listenHint"></span>
                    </div>

                    <div id="matchArea" style="display:none;"></div>
                    <div id="inputArea" style="display:none;"></div>

                    <div class="option-list" id="options"></div>

                    <div class="feedback" id="feedback"></div>

                    <div class="controls">
                        <button class="primary" id="nextBtn" onclick="nextQuestion()" disabled>다음 문제</button>
                        <button class="secondary" onclick="location.href='/usr/home/main'">메인으로 돌아가기</button>
                    </div>
                </div>

                <script>
                    const DEBUG = true;
                    const dlog = (...args) => { if (DEBUG) console.log(...args); };

                    function sendQuizResult(wordId, correct) {
                        if (!wordId || wordId < 0) return;
                        fetch('/usr/learning/record-quiz', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                            body: 'wordId=' + wordId + '&correct=' + correct
                        }).then(r => {
                            if (!r.ok) console.warn('Quiz result save failed');
                        }).catch(e => console.error(e));
                    }

                    function normalize(val) {
                        if (val === undefined || val === null) return '';
                        const trimmed = String(val).replace(/\r|\n/g, ' ').trim();
                        if (trimmed.toLowerCase() === 'null') return '';
                        return trimmed;
                    }

                    function isNonEmpty(s) {
                        return !!(s && String(s).trim());
                    }

                    function shuffle(arr) {
                        for (let i = arr.length - 1; i > 0; i--) {
                            const j = Math.floor(Math.random() * (i + 1));
                            [arr[i], arr[j]] = [arr[j], arr[i]];
                        }
                        return arr;
                    }

                    function escapeRegExp(str) {
                        return String(str).replace(/[.*+?^$()|[\]\\]/g, '\\$&');
                    }

                    const progressEl = document.getElementById('progress');
                    const questionEl = document.getElementById('question');
                    const optionsEl = document.getElementById('options');
                    const feedbackEl = document.getElementById('feedback');
                    const nextBtn = document.getElementById('nextBtn');

                    const meaningMetaEl = document.getElementById('meaningMeta');
                    const exampleMetaEl = document.getElementById('exampleMeta');

                    const listenBar = document.getElementById('listenBar');
                    const listenBtn = document.getElementById('listenBtn');
                    const listenHint = document.getElementById('listenHint');

                    const matchArea = document.getElementById('matchArea');
                    const inputArea = document.getElementById('inputArea');

                    const jsonTag = document.getElementById('wordsJsonData');

                    let words = [];
                    let questions = [];
                    let currentIndex = 0;
                    let answered = false;
                    let matchState = null;

                    function clearUI(msg) {
                        progressEl.textContent = msg || '';
                        questionEl.textContent = '';
                        optionsEl.innerHTML = '';
                        feedbackEl.textContent = '';

                        meaningMetaEl.textContent = '';
                        exampleMetaEl.textContent = '';

                        listenBar.style.display = 'none';
                        listenHint.textContent = '';
                        listenBtn.onclick = null;

                        matchArea.style.display = 'none';
                        matchArea.innerHTML = '';

                        inputArea.style.display = 'none';
                        inputArea.innerHTML = '';

                        nextBtn.disabled = true;
                        nextBtn.textContent = '다음 문제';

                        answered = false;
                        matchState = null;
                    }

                    function buildOptionsBasic(correctWord, direction) {
                        const targetField = direction === 'EN_TO_KO' ? 'ko' : 'en';
                        const others = words.filter(w => w.id !== correctWord.id);
                        shuffle(others);

                        const pool = [];
                        for (let i = 0; i < others.length && pool.length < 3; i++) {
                            const v = others[i][targetField];
                            if (isNonEmpty(v) && !pool.includes(v)) pool.push(v);
                        }

                        const correctVal = correctWord[targetField];
                        if (!pool.includes(correctVal)) pool.push(correctVal);

                        while (pool.length < 4) pool.push(correctVal);

                        const unique = [];
                        for (const p of pool) {
                            if (!unique.includes(p)) unique.push(p);
                        }
                        while (unique.length < 4) unique.push(correctVal);

                        return shuffle(unique.slice(0, 4));
                    }

                    function buildOptionsFromSpelling(correctSpelling) {
                        const others = words.filter(w => w.en !== correctSpelling);
                        shuffle(others);

                        const pool = [];
                        for (let i = 0; i < others.length && pool.length < 3; i++) {
                            const v = others[i].en;
                            if (isNonEmpty(v) && !pool.includes(v)) pool.push(v);
                        }

                        if (!pool.includes(correctSpelling)) pool.push(correctSpelling);
                        while (pool.length < 4) pool.push(correctSpelling);

                        const unique = [];
                        for (const p of pool) {
                            if (!unique.includes(p)) unique.push(p);
                        }
                        while (unique.length < 4) unique.push(correctSpelling);

                        return shuffle(unique.slice(0, 4));
                    }

                    function makeMcqMeaningQuestion(word) {
                        const direction = 'EN_TO_KO';
                        return {
                            type: 'MCQ_MEANING',
                            direction,
                            word,
                            prompt: word.en,
                            answer: word.ko,
                            options: buildOptionsBasic(word, direction)
                        };
                    }

                    function makeMcqWordQuestion(word) {
                        const direction = 'KO_TO_EN';
                        return {
                            type: 'MCQ_WORD',
                            direction,
                            word,
                            prompt: word.ko,
                            answer: word.en,
                            options: buildOptionsBasic(word, direction)
                        };
                    }

                    function makeClozeQuestion(word) {
                        if (!isNonEmpty(word.example)) return null;

                        const safe = escapeRegExp(word.en);
                        const re = new RegExp(safe, 'i');
                        const clozed = word.example.replace(re, '____');

                        if (clozed === word.example) return null;

                        return {
                            type: 'CLOZE',
                            word,
                            prompt: clozed,
                            answer: word.en,
                            options: buildOptionsFromSpelling(word.en)
                        };
                    }

                    function makeSpellingInputQuestion(word) {
                        return {
                            type: 'SPELLING_INPUT',
                            word,
                            prompt: word.ko,
                            answer: word.en
                        };
                    }

                    function makeScrambleQuestion(word) {
                        if (!isNonEmpty(word.en) || word.en.length <= 3) return null;

                        const chars = word.en.split('');
                        let scrambled = word.en;

                        for (let i = 0; i < 5; i++) {
                            scrambled = shuffle(chars.slice()).join('');
                            if (scrambled.toLowerCase() !== word.en.toLowerCase()) break;
                        }

                        return {
                            type: 'SCRAMBLE',
                            word,
                            prompt: scrambled,
                            answer: word.en
                        };
                    }

                    function makeListenMcqQuestion(word) {
                        if (!isNonEmpty(word.audioPath)) return null;

                        return {
                            type: 'LISTEN_MCQ',
                            word,
                            prompt: '발음을 듣고 맞는 단어를 고르세요.',
                            answer: word.en,
                            options: buildOptionsFromSpelling(word.en)
                        };
                    }

                    function makeMatchQuestion() {
                        if (words.length < 4) return null;

                        const pool = shuffle(words.slice());
                        const picked = pool.slice(0, Math.min(4, pool.length));

                        const left = picked.map(w => ({ id: w.id, text: w.en }));
                        const right = shuffle(picked.map(w => ({ id: w.id, text: w.ko })));

                        return {
                            type: 'MATCH',
                            words: picked,
                            prompt: '영어와 뜻을 짝지으세요.',
                            left,
                            right
                        };
                    }

                    function buildQuestionsMixed(limit) {
                        const total = Math.max(1, limit || words.length);

                        const hasExample = words.filter(w => isNonEmpty(w.example));
                        const hasAudio = words.filter(w => isNonEmpty(w.audioPath));
                        const longWords = words.filter(w => isNonEmpty(w.en) && w.en.length > 3);

                        const qs = [];
                        const shuffledWords = shuffle(words.slice());

                        // ✅ 1) 단어별 최소 1문제 보장 전략
                        // 우선 단어마다 기본 문제 하나씩
                        for (let i = 0; i < shuffledWords.length; i++) {
                            const w = shuffledWords[i];
                            // 의미/단어 랜덤 배치
                            if (Math.random() < 0.5) qs.push(makeMcqMeaningQuestion(w));
                            else qs.push(makeMcqWordQuestion(w));
                        }

                        // ✅ 2) 단어 수가 많을수록 다양한 유형 섞고 싶다면
                        // (선택) 일부를 다른 유형으로 치환
                        for (let i = 0; i < qs.length; i++) {
                            const w = qs[i].word;

                            // 예문 있으면 15% 확률로 cloze로 교체
                            if (hasExample.length && Math.random() < 0.15) {
                                const c = makeClozeQuestion(w);
                                if (c) qs[i] = c;
                            }

                            // 오디오 있으면 10% 확률로 listening 교체
                            if (hasAudio.length && Math.random() < 0.10) {
                                const l = makeListenMcqQuestion(w);
                                if (l) qs[i] = l;
                            }

                            // 10% 확률로 스펠링/스크램블 계열 교체
                            if (Math.random() < 0.10) {
                                qs[i] = makeSpellingInputQuestion(w);
                            }
                        }

                        // ✅ 3) MATCH는 단어가 충분할 때만 "추가 1문제"
                        if (words.length >= 4 && qs.length < total + 1 && Math.random() < 0.25) {
                            const m = makeMatchQuestion();
                            if (m) qs.push(m);
                        }

                        // ✅ 4) 최종 길이 보정
                        return shuffle(qs).slice(0, total);
                    }

                    function renderMcq(q) {
                        optionsEl.innerHTML = '';
                        q.options.forEach(label => {
                            const btn = document.createElement('button');
                            btn.className = 'option-btn';
                            btn.textContent = label;
                            btn.onclick = () => selectMcq(btn, label, q);
                            optionsEl.appendChild(btn);
                        });
                    }

                    function selectMcq(button, chosen, q) {
                        if (answered) return;
                        answered = true;

                        const buttons = optionsEl.querySelectorAll('.option-btn');
                        buttons.forEach(btn => {
                            btn.disabled = true;
                            if (btn.textContent === q.answer) btn.classList.add('correct');
                        });

                        if (chosen === q.answer) {
                            feedbackEl.textContent = '정답입니다!';
                            feedbackEl.style.color = '#2e7d32';
                            sendQuizResult(q.word.id, true);
                        } else {
                            feedbackEl.textContent = '틀렸어요. 정답은 "' + q.answer + '" 입니다.';
                            feedbackEl.style.color = '#c62828';
                            button.classList.add('wrong');
                            sendQuizResult(q.word.id, false);
                        }

                        nextBtn.disabled = false;
                    }

                    function renderInput(q, placeholderText) {
                        inputArea.style.display = 'block';
                        optionsEl.innerHTML = '';

                        const wrap = document.createElement('div');
                        wrap.className = 'input-area';

                        const input = document.createElement('input');
                        input.className = 'answer-input';
                        input.type = 'text';
                        input.placeholder = placeholderText || '';
                        input.autocomplete = 'off';

                        const submit = document.createElement('button');
                        submit.className = 'submit-btn';
                        submit.textContent = '제출';

                        function doSubmit() {
                            if (answered) return;

                            const userText = normalize(input.value);
                            if (!userText) {
                                feedbackEl.textContent = '답을 입력해주세요.';
                                feedbackEl.style.color = '#c62828';
                                return;
                            }

                            answered = true;
                            input.disabled = true;
                            submit.disabled = true;

                            const correct = normalize(q.answer).toLowerCase();
                            const typed = userText.toLowerCase();

                            if (typed === correct) {
                                feedbackEl.textContent = '정답입니다!';
                                feedbackEl.style.color = '#2e7d32';
                                sendQuizResult(q.word.id, true);
                            } else {
                                feedbackEl.textContent = '틀렸어요. 정답은 "' + q.answer + '" 입니다.';
                                feedbackEl.style.color = '#c62828';
                                sendQuizResult(q.word.id, false);
                            }

                            nextBtn.disabled = false;
                        }

                        submit.onclick = doSubmit;
                        input.addEventListener('keydown', function (e) {
                            if (e.key === 'Enter') doSubmit();
                        });

                        wrap.appendChild(input);
                        wrap.appendChild(submit);

                        inputArea.innerHTML = '';
                        inputArea.appendChild(wrap);
                    }

                    function renderMatch(q) {
                        matchArea.style.display = 'block';
                        optionsEl.innerHTML = '';

                        matchState = {
                            leftSelected: null,
                            matchedLeft: new Set(),
                            matchedRight: new Set()
                        };

                        const wrap = document.createElement('div');
                        wrap.className = 'match-wrap';

                        const leftCol = document.createElement('div');
                        leftCol.className = 'match-col';

                        const rightCol = document.createElement('div');
                        rightCol.className = 'match-col';

                        const leftItems = [];
                        const rightItems = [];

                        q.left.forEach((item, i) => {
                            const el = document.createElement('div');
                            el.className = 'match-item';
                            el.textContent = item.text;
                            el.onclick = function () { onPickLeft(i); };
                            leftCol.appendChild(el);
                            leftItems.push(el);
                        });

                        q.right.forEach((item, j) => {
                            const el = document.createElement('div');
                            el.className = 'match-item';
                            el.textContent = item.text;
                            el.onclick = function () { onPickRight(j); };
                            rightCol.appendChild(el);
                            rightItems.push(el);
                        });

                        wrap.appendChild(leftCol);
                        wrap.appendChild(rightCol);

                        matchArea.innerHTML = '';
                        matchArea.appendChild(wrap);

                        function refreshSelection() {
                            leftItems.forEach((el, i) => {
                                el.classList.remove('selected');
                                if (matchState.leftSelected === i) el.classList.add('selected');
                            });
                        }

                        function flashWrong(a, b) {
                            a.classList.add('wrongflash');
                            b.classList.add('wrongflash');
                            setTimeout(function () {
                                a.classList.remove('wrongflash');
                                b.classList.remove('wrongflash');
                            }, 180);
                        }

                        function markMatched(li, rj) {
                            matchState.matchedLeft.add(li);
                            matchState.matchedRight.add(rj);

                            leftItems[li].classList.remove('selected');
                            leftItems[li].classList.add('matched');
                            rightItems[rj].classList.add('matched');

                            leftItems[li].onclick = null;
                            rightItems[rj].onclick = null;
                        }

                        function checkComplete() {
                            if (matchState.matchedLeft.size === q.left.length) {
                                feedbackEl.textContent = '모두 맞췄습니다!';
                                feedbackEl.style.color = '#2e7d32';
                                answered = true;
                                nextBtn.disabled = false;
                            }
                        }

                        function onPickLeft(i) {
                            if (matchState.matchedLeft.has(i)) return;
                            matchState.leftSelected = i;
                            refreshSelection();
                        }

                        function onPickRight(j) {
                            if (matchState.matchedRight.has(j)) return;
                            if (matchState.leftSelected === null) return;

                            const li = matchState.leftSelected;
                            const leftId = q.left[li].id;
                            const rightId = q.right[j].id;

                            if (leftId === rightId) {
                                markMatched(li, j);
                                feedbackEl.textContent = '좋아요! 계속 짝지어보세요.';
                                feedbackEl.style.color = '#2e7d32';
                                sendQuizResult(leftId, true);
                            } else {
                                flashWrong(leftItems[li], rightItems[j]);
                                feedbackEl.textContent = '틀린 짝입니다.';
                                feedbackEl.style.color = '#c62828';
                                sendQuizResult(leftId, false);
                            }

                            matchState.leftSelected = null;
                            refreshSelection();
                            checkComplete();
                        }

                        // MATCH 힌트는 메타에 살짝
                        meaningMetaEl.textContent = '';
                        exampleMetaEl.textContent = '힌트: 왼쪽 영어를 고르고 오른쪽 뜻을 선택하세요.';
                    }

                    function render() {
                        if (!questions || !questions.length) {
                            return clearUI('출제할 문제가 없습니다.');
                        }
                        if (currentIndex >= questions.length) {
                            return clearUI('오늘의 퀴즈를 완료했습니다!');
                        }

                        const q = questions[currentIndex];

                        answered = false;
                        nextBtn.disabled = true;
                        nextBtn.textContent = (currentIndex === questions.length - 1) ? '완료' : '다음 문제';

                        optionsEl.innerHTML = '';
                        feedbackEl.textContent = '';
                        feedbackEl.style.color = '';

                        meaningMetaEl.textContent = '';
                        exampleMetaEl.textContent = '';

                        listenBar.style.display = 'none';
                        listenBtn.onclick = null;
                        listenHint.textContent = '';

                        matchArea.style.display = 'none';
                        matchArea.innerHTML = '';
                        matchState = null;

                        inputArea.style.display = 'none';
                        inputArea.innerHTML = '';

                        /* ✅ 타입 표시 제거 */
                        progressEl.textContent = '문제 ' + (currentIndex + 1) + ' / ' + questions.length;

                        // 공통 예문 세팅(단, CLOZE는 별도)
                        function setExampleFromWord() {
                            if (q.word && isNonEmpty(q.word.example)) {
                                exampleMetaEl.textContent = '예문: ' + q.word.example;
                            } else if (q.word) {
                                exampleMetaEl.textContent = '예문이 없습니다.';
                            }
                        }

                        if (q.type === 'MCQ_MEANING') {
                            questionEl.textContent = '영어 "' + q.prompt + '"의 뜻을 고르세요.';
                            meaningMetaEl.textContent = '';
                            setExampleFromWord();
                            renderMcq(q);
                        }
                        else if (q.type === 'MCQ_WORD') {
                            questionEl.textContent = '한글 "' + q.prompt + '"에 해당하는 영어 단어를 고르세요.';
                            meaningMetaEl.textContent = '';
                            setExampleFromWord();
                            renderMcq(q);
                        }
                        else if (q.type === 'CLOZE') {
                            questionEl.textContent = '빈칸에 들어갈 가장 알맞은 단어는?';
                            meaningMetaEl.textContent = '';
                            exampleMetaEl.textContent = '문장: ' + q.prompt;
                            renderMcq(q);
                        }
                        else if (q.type === 'SPELLING_INPUT') {
                            questionEl.textContent = '뜻을 보고 영어 철자를 입력하세요.';
                            meaningMetaEl.textContent = '뜻: ' + q.prompt;
                            exampleMetaEl.textContent = '';
                            renderInput(q, '뜻 힌트로 영어 입력');
                        }
                        else if (q.type === 'SCRAMBLE') {
                            questionEl.textContent = '섞인 글자를 원래 단어로 바꾸세요.';
                            meaningMetaEl.textContent = '섞인 철자: ' + q.prompt;
                            exampleMetaEl.textContent = '';
                            renderInput(q, '원래 단어 입력');
                        }
                        else if (q.type === 'LISTEN_MCQ') {
                            questionEl.textContent = q.prompt;

                            listenBar.style.display = 'flex';
                            listenHint.textContent = '재생 버튼을 눌러 발음을 들으세요.';

                            listenBtn.onclick = function () {
                                try {
                                    const audio = new Audio(q.word.audioPath);
                                    audio.play();
                                } catch (e) {
                                    console.warn('audio play fail', e);
                                }
                            };

                            meaningMetaEl.textContent = '';
                            setExampleFromWord();
                            renderMcq(q);
                        }
                        else if (q.type === 'MATCH') {
                            questionEl.textContent = q.prompt;
                            // meaning/example은 renderMatch에서 힌트로 처리
                            renderMatch(q);
                        }
                        else {
                            questionEl.textContent = '문제를 불러올 수 없습니다.';
                            nextBtn.disabled = false;
                        }
                    }

                    window.nextQuestion = function () {
                        if (!questions || !questions.length) {
                            return clearUI('출제할 문제가 없습니다.');
                        }
                        if (currentIndex >= questions.length - 1) {
                            return clearUI('오늘의 퀴즈를 완료했습니다!');
                        }
                        currentIndex += 1;
                        render();
                    };

                    (function init() {
                        if (!jsonTag) {
                            clearUI('퀴즈 데이터를 불러오지 못했습니다.');
                            console.error('wordsJsonData 태그가 없습니다.');
                            return;
                        }

                        const rawJson = jsonTag.textContent || '[]';

                        let baseWords = [];
                        try {
                            baseWords = JSON.parse(rawJson);
                        } catch (e) {
                            console.error('퀴즈 데이터 파싱 실패', e, rawJson);
                            baseWords = [];
                        }

                        words = baseWords
                            .map(w => {
                                const en = normalize(w.spelling);
                                const ko = normalize(w.meaning);
                                const example = normalize(w.exampleSentence ?? w.example);
                                const audioPath = normalize(w.audioPath ?? w.audio);
                                return {
                                    id: (w.id ?? -1),
                                    en,
                                    ko,
                                    example,
                                    audioPath
                                };
                            })
                            .filter(w => isNonEmpty(w.en) && isNonEmpty(w.ko));

                        if (!words.length) {
                            clearUI('학습할 단어가 부족합니다.');
                            return;
                        }

                        questions = buildQuestionsMixed(words.length);
                        render();
                    })();
                </script>
            </c:if>
        </body>

        </html>