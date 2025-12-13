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
    <!-- 서버가 내려준 문제/단어 JSON -->
    <script type="application/json" id="questionsJsonData">
        <c:out value="${questionsJson}" escapeXml="false"/>
    </script>
    <script type="application/json" id="wordsJsonData">
        <c:out value="${wordsJson}" escapeXml="false"/>
    </script>

    <div class="quiz-card">
        <div class="progress" id="progress"></div>
        <div class="meta" id="quizStats"></div> <!-- 이제는 내부에서 비움 -->
        <div class="question" id="question"></div>

        <div class="meta" id="meaningMeta"></div>
        <div class="meta" id="exampleMeta"></div>

        <div class="listen-bar" id="listenBar" style="display:none;">
            <button class="listen-btn" id="listenBtn">발음 재생</button>
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

        // 서버에서 받은 "오늘 푼/남은" 통계
        const INITIAL_SOLVED = Number('${quizSolvedCount != null ? quizSolvedCount : 0}');
        const INITIAL_REMAINING = Number('${quizRemainingCount != null ? quizRemainingCount : 0}');

        // ---- DOM 캐시 ----
        const progressEl = document.getElementById('progress');
        const quizStatsEl = document.getElementById('quizStats');
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

        const wordsTag = document.getElementById('wordsJsonData');
        const questionsTag = document.getElementById('questionsJsonData');

        // ---- 상태 ----
        let words = [];
        let questions = [];
        let currentIndex = 0;
        let answered = false;
        let matchState = null;

        // 오늘 푼 / 남은
        let solvedCount = Number.isFinite(INITIAL_SOLVED) ? Math.max(0, INITIAL_SOLVED) : 0;
        let remainingCount = Number.isFinite(INITIAL_REMAINING) ? Math.max(0, INITIAL_REMAINING) : 0;

        // 상단 한 줄 렌더: "문제 3 / 50 · 오늘 푼: 12개 / 남은 문제: 38개"
        function renderHeader() {
            if (!progressEl) return;

            const total = Array.isArray(questions) ? questions.length : 0;
            let current = 0;
            if (total > 0 && currentIndex >= 0 && currentIndex < total) {
                current = currentIndex + 1;
            }

            progressEl.textContent =
                '문제 ' + current + ' / ' + total +
                ' · 오늘 푼: ' + solvedCount + '개 / 남은 문제: ' + remainingCount + '개';

            if (quizStatsEl) {
                quizStatsEl.textContent = '';
            }
        }

        function updateQuizStats(solved, remaining) {
            if (typeof solved === 'number' && !Number.isNaN(solved)) {
                solvedCount = Math.max(0, solved);
            }
            if (typeof remaining === 'number' && !Number.isNaN(remaining)) {
                remainingCount = Math.max(0, remaining);
            }
            renderHeader();
        }

        // ---- 유틸 ----
        function normalize(val) {
            if (val === undefined || val === null) return '';
            const trimmed = String(val).replace(/\r|\n/g, ' ').trim();
            if (trimmed.toLowerCase() === 'null') return '';
            return trimmed;
        }

        function isNonEmpty(s) {
            return !!(s && String(s).trim());
        }

        function hasHangul(text) {
            if (!isNonEmpty(text)) return false;
            return /[\u3131-\uD79D]/.test(text);
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

        function readJson(tag) {
            if (!tag) return null;
            const raw = tag.textContent || '';
            if (!raw.trim()) return null;
            try {
                return JSON.parse(raw);
            } catch (e) {
                console.error('JSON 파싱 실패', e, raw);
                return null;
            }
        }

        // ---- 서버/폴백 데이터 정리 ----
        function hydrateWords(rawList) {
            if (!Array.isArray(rawList)) return [];
            return rawList
                .map(w => {
                    const en = normalize(w.spelling ?? w.en);
                    const ko = normalize(w.meaning ?? w.ko);
                    const example = normalize(w.exampleSentence ?? w.example);
                    const audioPath = normalize(w.audioPath ?? w.audio);
                    return {
                        id: (typeof w.id === 'number' ? w.id : -1),
                        en,
                        ko,
                        example,
                        audioPath
                    };
                })
                .filter(w => isNonEmpty(w.en) && isNonEmpty(w.ko));
        }

        function needsOptions(type) {
            const t = String(type || '').toUpperCase();
            return t === 'MCQ_MEANING' || t === 'MCQ_WORD' || t === 'CLOZE' || t === 'LISTEN_MCQ';
        }

        function isValidQuestion(q) {
            if (!q) return false;
            const type = String(q.type || '').toUpperCase();
            const prompt = normalize(q.prompt);
            const correct = normalize(q.correct ?? q.answer);
            if (!isNonEmpty(prompt) || !isNonEmpty(correct)) return false;

            if (type === 'MATCH') {
                return Array.isArray(q.left) && q.left.length &&
                    Array.isArray(q.right) && q.right.length;
            }

            if (needsOptions(type)) {
                if (!Array.isArray(q.options) || q.options.length !== 4) return false;
                return q.options.every(opt => isNonEmpty(normalize(opt)));
            }
            return true;
        }

        function normalizeDirection(raw, fallback) {
            const base = String(raw ?? '').trim().toUpperCase();
            if (base === 'EN_TO_KO' || base === 'KO_TO_EN') return base;
            return fallback;
        }

        function hydrateServerQuestions(rawList) {
            if (!Array.isArray(rawList)) return [];
            return rawList
                .map(item => {
                    if (!item) return null;
                    const type = String(item.type || 'MCQ_MEANING').toUpperCase();

                    const q = {
                        wordId: typeof item.wordId === 'number'
                            ? item.wordId
                            : (typeof item.id === 'number' ? item.id : -1),
                        type,
                        direction: normalizeDirection(
                            item.direction,
                            type === 'MCQ_WORD' ? 'KO_TO_EN' : 'EN_TO_KO'
                        ),
                        prompt: normalize(item.prompt),
                        answer: normalize(item.answer ?? item.correct),
                        correct: normalize(item.correct ?? item.answer),
                        example: normalize(item.example),
                        audioPath: normalize(item.audioPath),
                        spelling: normalize(item.spelling),
                        meaning: normalize(item.meaning)
                    };

                    if (Array.isArray(item.options)) {
                        const opts = item.options.map(o => normalize(o)).filter(isNonEmpty);
                        if (opts.length || needsOptions(type)) q.options = opts;
                    }

                    if (Array.isArray(item.left) && Array.isArray(item.right)) {
                        q.left = item.left.map(e => ({
                            id: e.id,
                            text: normalize(e.text)
                        }));
                        q.right = item.right.map(e => ({
                            id: e.id,
                            text: normalize(e.text)
                        }));
                    }

                    if (item.word) {
                        q.word = {
                            example: normalize(item.word.example ?? item.word.exampleSentence),
                            audioPath: normalize(item.word.audioPath ?? item.word.audio),
                            en: normalize(item.word.spelling ?? item.word.en),
                            ko: normalize(item.word.meaning ?? item.word.ko)
                        };
                    }

                    return q;
                })
                .filter(isValidQuestion);
        }

        function getQuestionWordId(q) {
            if (q && typeof q.wordId === 'number') return q.wordId;
            if (q && typeof q.id === 'number') return q.id;
            return -1;
        }

        function getWordMeta(q) {
            const word = q && q.word ? q.word : {};
            return {
                example: normalize(word.example ?? q.example ?? ''),
                audioPath: normalize(word.audioPath ?? q.audioPath ?? ''),
                meaning: normalize(word.ko ?? q.meaning ?? ''),
                spelling: normalize(word.en ?? q.spelling ?? '')
            };
        }

        // ---- 폴백용 로컬 문제 생성 ----
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
            for (const p of pool) if (!unique.includes(p)) unique.push(p);
            while (unique.length < 4) unique.push(correctSpelling);

            return shuffle(unique.slice(0, 4));
        }

        function buildOptionsByDirection(word, direction) {
            const dir = normalizeDirection(direction, 'EN_TO_KO');
            const target = dir === 'KO_TO_EN' ? 'en' : 'ko';
            const correctVal = normalize(word[target]);
            const pool = [];

            const push = v => {
                const t = normalize(v);
                if (!isNonEmpty(t)) return;
                if (pool.includes(t)) return;
                pool.push(t);
            };

            push(correctVal);
            const others = words.filter(w => w.id !== word.id);
            shuffle(others);
            for (let i = 0; i < others.length && pool.length < 4; i++) {
                push(others[i][target]);
            }
            while (pool.length < 4) push(correctVal);

            return shuffle(pool.slice(0, 4));
        }

        function makeMcqMeaningQuestion(word) {
            return {
                id: word.id,
                type: 'MCQ_MEANING',
                direction: 'EN_TO_KO',
                word,
                prompt: normalize(word.en),
                answer: normalize(word.ko),
                correct: normalize(word.ko),
                options: buildOptionsByDirection(word, 'EN_TO_KO')
            };
        }

        function makeMcqWordQuestion(word) {
            return {
                id: word.id,
                type: 'MCQ_WORD',
                direction: 'KO_TO_EN',
                word,
                prompt: normalize(word.ko),
                answer: normalize(word.en),
                correct: normalize(word.en),
                options: buildOptionsByDirection(word, 'KO_TO_EN')
            };
        }

        function makeMcqAuto(word) {
            const en = normalize(word.en);
            const ko = normalize(word.ko);
            if (!isNonEmpty(en) || !isNonEmpty(ko)) return null;
            return hasHangul(ko) ? makeMcqWordQuestion(word) : makeMcqMeaningQuestion(word);
        }

        function makeClozeQuestion(word) {
            if (!isNonEmpty(word.example)) return null;
            const safe = escapeRegExp(word.en);
            const placeholder = '____';
            const regex = new RegExp(safe, 'ig');
            const clozed = word.example.replace(regex, placeholder);
            if (clozed === word.example) return null;

            return {
                id: word.id,
                type: 'CLOZE',
                word,
                prompt: clozed,
                answer: word.en,
                correct: word.en,
                options: buildOptionsFromSpelling(word.en)
            };
        }

        function makeSpellingInputQuestion(word) {
            if (!hasHangul(word.ko)) return null;
            return {
                id: word.id,
                type: 'SPELLING_INPUT',
                word,
                prompt: word.ko,
                answer: word.en,
                correct: word.en
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
            if (scrambled.toLowerCase() === word.en.toLowerCase()) return null;

            return {
                id: word.id,
                type: 'SCRAMBLE',
                word,
                prompt: scrambled,
                answer: word.en,
                correct: word.en
            };
        }

        function makeListenMcqQuestion(word) {
            if (!isNonEmpty(word.audioPath)) return null;
            return {
                id: word.id,
                type: 'LISTEN_MCQ',
                word,
                prompt: '발음을 듣고 맞는 단어를 고르세요.',
                answer: word.en,
                correct: word.en,
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

            const qs = [];
            const shuffledWords = shuffle(words.slice());

            // 기본 MCQ 한 문제씩
            for (let i = 0; i < shuffledWords.length; i++) {
                const w = shuffledWords[i];
                const mcq = makeMcqAuto(w);
                if (mcq) qs.push(mcq);
            }

            // 일부를 다른 유형으로 치환
            for (let i = 0; i < qs.length; i++) {
                const w = qs[i].word;
                const roll = Math.random();

                if (hasExample.length && roll < 0.15) {
                    const c = makeClozeQuestion(w);
                    if (c) { qs[i] = c; continue; }
                }
                if (hasAudio.length && roll >= 0.15 && roll < 0.25) {
                    const l = makeListenMcqQuestion(w);
                    if (l) { qs[i] = l; continue; }
                }
                if (roll >= 0.25 && roll < 0.35) {
                    const s = makeSpellingInputQuestion(w);
                    if (s) { qs[i] = s; continue; }
                }
                if (roll >= 0.35 && roll < 0.45) {
                    const sc = makeScrambleQuestion(w);
                    if (sc) qs[i] = sc;
                }
            }

            if (words.length >= 4 && qs.length < total + 1 && Math.random() < 0.25) {
                const m = makeMatchQuestion();
                if (m) qs.push(m);
            }

            return shuffle(qs).slice(0, total);
        }

        // ---- 정답 전송 ----
        function sendQuizResult(wordId, correct) {
            if (!wordId || wordId < 0) return;
            const form = new URLSearchParams();
            form.append('wordId', wordId);
            form.append('correct', correct);

            fetch('/learning/quiz/result', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                credentials: 'same-origin',
                body: form.toString()
            })
                .then(r => r.json().catch(() => null))
                .then(data => {
                    if (!data || !data.success) {
                        const msg = data && data.msg === 'NOT_LOGGED_IN'
                            ? '세션이 만료되었습니다. 다시 로그인해 주세요.'
                            : '결과 저장에 실패했습니다. 잠시 후 다시 시도해 주세요.';
                        alert(msg);
                        return;
                    }
                    updateQuizStats(data.quizSolvedCount, data.quizRemainingCount);
                })
                .catch(e => {
                    console.error(e);
                    alert('결과 저장 중 오류가 발생했습니다.');
                });
        }

        // ---- 렌더링 ----
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
            const correctAnswer = q.correct ?? q.answer;
            buttons.forEach(btn => {
                btn.disabled = true;
                if (btn.textContent === correctAnswer) btn.classList.add('correct');
            });

            if (chosen === correctAnswer) {
                feedbackEl.textContent = '정답입니다!';
                feedbackEl.style.color = '#2e7d32';
                sendQuizResult(getQuestionWordId(q), true);
            } else {
                feedbackEl.textContent = '틀렸어요. 정답은 "' + correctAnswer + '" 입니다.';
                feedbackEl.style.color = '#c62828';
                button.classList.add('wrong');
                sendQuizResult(getQuestionWordId(q), false);
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

                const correct = normalize(q.correct ?? q.answer).toLowerCase();
                const typed = userText.toLowerCase();

                if (typed === correct) {
                    feedbackEl.textContent = '정답입니다!';
                    feedbackEl.style.color = '#2e7d32';
                    sendQuizResult(getQuestionWordId(q), true);
                } else {
                    feedbackEl.textContent = '틀렸어요. 정답은 "' + q.answer + '" 입니다.';
                    feedbackEl.style.color = '#c62828';
                    sendQuizResult(getQuestionWordId(q), false);
                }

                nextBtn.disabled = false;
            }

            submit.onclick = doSubmit;
            input.addEventListener('keydown', e => {
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
                el.onclick = () => onPickLeft(i);
                leftCol.appendChild(el);
                leftItems.push(el);
            });

            q.right.forEach((item, j) => {
                const el = document.createElement('div');
                el.className = 'match-item';
                el.textContent = item.text;
                el.onclick = () => onPickRight(j);
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
                setTimeout(() => {
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
            const meta = getWordMeta(q);

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

            // 상단 헤더 갱신
            renderHeader();

            // 예문 메타 셋업
            function setExampleFromMeta() {
                if (isNonEmpty(meta.example)) {
                    exampleMetaEl.textContent = '예문: ' + meta.example;
                } else {
                    exampleMetaEl.textContent = '';
                }
            }

            if (q.type === 'MCQ_MEANING') {
                const promptText = normalize(q.prompt);
                questionEl.textContent = '영어 "' + promptText + '"의 뜻을 고르세요.';
                setExampleFromMeta();
                renderMcq(q);
            } else if (q.type === 'MCQ_WORD') {
                const promptText = normalize(q.prompt);
                questionEl.textContent = '한글 "' + promptText + '"에 해당하는 영어 단어를 고르세요.';
                setExampleFromMeta();
                renderMcq(q);
            } else if (q.type === 'CLOZE') {
                questionEl.textContent = '빈칸에 들어갈 가장 알맞은 단어는?';
                meaningMetaEl.textContent = '';
                exampleMetaEl.textContent = '문장: ' + q.prompt;
                renderMcq(q);
            } else if (q.type === 'SPELLING_INPUT') {
                questionEl.textContent = '뜻을 보고 영어 철자를 입력하세요.';
                meaningMetaEl.textContent = '뜻: ' + q.prompt;
                exampleMetaEl.textContent = '';
                renderInput(q, '뜻 힌트로 영어 입력');
            } else if (q.type === 'SCRAMBLE') {
                questionEl.textContent = '섞인 글자를 원래 단어로 바꾸세요.';
                meaningMetaEl.textContent = '섞인 철자: ' + q.prompt;
                exampleMetaEl.textContent = '';
                renderInput(q, '원래 단어 입력');
            } else if (q.type === 'LISTEN_MCQ') {
                questionEl.textContent = isNonEmpty(q.prompt)
                    ? q.prompt
                    : '발음을 듣고 단어를 고르세요.';

                const audioSrc = meta.audioPath;
                if (isNonEmpty(audioSrc)) {
                    listenBar.style.display = 'flex';
                    listenHint.textContent = '재생 버튼을 눌러 발음을 들으세요.';
                    listenBtn.onclick = () => {
                        try {
                            new Audio(audioSrc).play();
                        } catch (e) {
                            console.warn('audio play fail', e);
                        }
                    };
                }

                setExampleFromMeta();
                renderMcq(q);
            } else if (q.type === 'MATCH') {
                questionEl.textContent = q.prompt;
                renderMatch(q);
            } else {
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

        // ---- 부트스트랩 ----
        function bootstrapFromServerPayload() {
            const rawQuestions = readJson(questionsTag) || [];
            const serverQuestions = hydrateServerQuestions(rawQuestions);
            if (!serverQuestions.length) return false;

            questions = serverQuestions;

            // 이미 푼 개수만큼 건너뛰기
            let initialSolved = Number.isFinite(INITIAL_SOLVED) ? INITIAL_SOLVED : 0;
            if (initialSolved < 0) initialSolved = 0;
            if (initialSolved > questions.length) initialSolved = questions.length;

            currentIndex = initialSolved;

            let initialRemaining;
            if (Number.isFinite(INITIAL_REMAINING)) {
                initialRemaining = Math.max(0, INITIAL_REMAINING);
            } else {
                initialRemaining = Math.max(questions.length - initialSolved, 0);
            }

            updateQuizStats(initialSolved, initialRemaining);

            if (initialRemaining <= 0 || currentIndex >= questions.length) {
                clearUI('오늘의 퀴즈를 완료했습니다!');
                return true;
            }

            render();
            return true;
        }

        function bootstrapFromFallbackWords() {
            if (!wordsTag) {
                clearUI('퀴즈 데이터를 불러오지 못했습니다.');
                console.error('wordsJsonData 태그가 없습니다.');
                return false;
            }
            const rawWords = readJson(wordsTag) || [];
            words = hydrateWords(rawWords);
            if (!words.length) {
                clearUI('학습할 단어가 부족합니다.');
                return false;
            }

            currentIndex = 0;
            const generated = buildQuestionsMixed(words.length).filter(isValidQuestion);
            if (!generated.length) {
                clearUI('출제할 수 있는 문제가 없습니다.');
                return false;
            }

            questions = generated;
            updateQuizStats(0, questions.length);
            render();
            return true;
        }

        (function init() {
            const ok = bootstrapFromServerPayload();
            if (!ok) {
                bootstrapFromFallbackWords();
            }
        })();
    </script>
</c:if>

</body>
</html>
