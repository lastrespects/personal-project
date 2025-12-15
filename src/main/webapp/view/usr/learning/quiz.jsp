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

                button {
                    cursor: pointer;
                    padding: 10px 16px;
                    border-radius: 8px;
                    border: 1px solid #ccc;
                    background: #fff;
                }

                button:hover {
                    background: #f9f9f9;
                    border-color: #aaa;
                }

                button:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                }

                #nextBtn {
                    background: #2c6bed;
                    color: #fff;
                    border: none;
                }

                #nextBtn:hover {
                    background: #1b5ac2;
                }

                .quiz-card {
                    border: 1px solid #ddd;
                    border-radius: 12px;
                    padding: 18px;
                    max-width: 760px;
                    background: #fafafa;
                }

                .option-btn {
                    width: 100%;
                    text-align: left;
                    margin-bottom: 8px;
                    transition: all 0.2s;
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
                    user-select: none;
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
                    pointer-events: none;
                }

                .match-item.wrongflash {
                    border-color: #f44336;
                    background: #ffecec;
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

                .top-bar {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 12px;
                }
            </style>
        </head>

        <body>
            <div id="quiz-root" data-target="${dailyTarget}">
                <div class="top-bar">
                    <h1>오늘의 퀴즈</h1>
                    <button type="button"
                        onclick="location.href='${pageContext.request.contextPath}/usr/home/main'">메인으로</button>
                </div>

                <p>오늘 학습할 단어는 총 <b id="targetCount">${dailyTarget}</b>개입니다.</p>

                <div style="margin:12px 0;">
                    진행: <b id="progressText">0 / 0</b> (남은 문제: <b id="remainingCount">${quizRemainingCount}</b>)
                    <br>
                    정답: <b id="correctCount">${quizCorrectCount}</b> / <b id="solvedCount">${quizSolvedCount}</b>
                </div>

                <div id="card" class="quiz-card">
                    <div id="qType" style="font-size:12px; opacity:.7; margin-bottom:8px;"></div>
                    <div id="prompt" style="font-size:20px; font-weight:700; margin-bottom:12px;"></div>
                    <div id="sub" style="font-size:14px; opacity:.85; margin-bottom:12px;"></div>

                    <div id="options" style="display:flex; flex-direction:column; gap:10px;"></div>
                    <div id="matchArea" style="display:none;"></div>
                    <div id="inputArea" style="display:none;"></div>

                    <div id="feedback" style="min-height: 22px; margin-top: 6px; font-weight: bold;"></div>

                    <div style="margin-top:14px;">
                        <button id="nextBtn" type="button" disabled>다음 문제</button>
                    </div>
                </div>

                <div id="done"
                    style="display:none; margin-top:16px; border:1px solid #ddd; border-radius:12px; padding:18px; max-width:760px;">
                    <div style="font-size:18px; font-weight:800;">오늘의 퀴즈를 완료했습니다!</div>
                    <div style="margin-top:10px;">최종 정답: <b id="doneCorrect">0</b> / <b id="doneTotal">0</b></div>
                    <div style="margin-top:14px;">
                        <button type="button"
                            onclick="location.href='${pageContext.request.contextPath}/usr/home/main'">
                            메인으로 돌아가기
                        </button>
                    </div>
                </div>
            </div>

            <!-- 서버에서 내려주는 단어목록 JSON -->
            <script id="words-json" type="application/json"><c:out value="${wordsJson}" escapeXml="false" /></script>

            <script>
                (() => {
                    const base = "${pageContext.request.contextPath}";
                    const root = document.getElementById("quiz-root");
                    const target = Number(root?.dataset?.target || 0);

                    // UI
                    const elType = document.getElementById("qType");
                    const elPrompt = document.getElementById("prompt");
                    const elSub = document.getElementById("sub");
                    const elOptions = document.getElementById("options");
                    const elMatchArea = document.getElementById("matchArea");
                    const elInputArea = document.getElementById("inputArea");
                    const elFeedback = document.getElementById("feedback");
                    const elNext = document.getElementById("nextBtn");

                    const elProgressText = document.getElementById("progressText");
                    const elRemaining = document.getElementById("remainingCount");
                    const elSolved = document.getElementById("solvedCount");
                    const elCorrect = document.getElementById("correctCount");

                    const doneBox = document.getElementById("done");
                    const cardBox = document.getElementById("card");
                    const doneCorrect = document.getElementById("doneCorrect");
                    const doneTotal = document.getElementById("doneTotal");

                    // 서버 값
                    let solved = ${ quizSolvedCount };
                    let correct = ${ quizCorrectCount };
                    let remaining = ${ quizRemainingCount };

                    // ✅ localStorage/sessionStorage 일절 사용하지 않음 (storage 에러 유발 방지)

                    // ---------- wordsJson 파싱 ----------
                    const hasHangul = (t) => /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(String(t || ""));
                    const hasEnglish = (t) => /[A-Za-z]/.test(String(t || ""));
                    const clean = (v) => {
                        const s = String(v ?? "").trim();
                        if (!s || s === "false" || s === "null" || s === "undefined") return "";
                        return s;
                    };

                    let rawWords = [];
                    try {
                        const raw = document.getElementById("words-json")?.textContent || "[]";
                        rawWords = JSON.parse(raw);
                    } catch (e) {
                        console.error("wordsJson 파싱 실패", e);
                        rawWords = [];
                    }

                    // ---------- 정규화(스왑/빈뜻 방지/EN-KO 추출) ----------
                    function normalizeWord(w) {
                        let spelling = clean(w.spelling);
                        let meaning = clean(w.meaning);
                        const exampleSentence = clean(w.exampleSentence);
                        let audioPath = clean(w.audioPath);
                        const id = w.id;

                        // 상대경로 오디오면 contextPath 붙이기
                        if (audioPath && !audioPath.startsWith("http") && !audioPath.startsWith(base)) {
                            audioPath = base + (audioPath.startsWith("/") ? "" : "/") + audioPath;
                        }

                        // 뒤집힘 자동복구: spelling이 한글이고 meaning이 영어이면 swap
                        if (spelling && meaning && hasHangul(spelling) && hasEnglish(meaning) && !hasHangul(meaning)) {
                            const tmp = spelling; spelling = meaning; meaning = tmp;
                        }

                        // "영어 단어" / "한글 뜻"을 최대한 뽑아내기
                        const en = (hasEnglish(spelling) ? spelling : (hasEnglish(meaning) ? meaning : spelling || meaning));
                        const ko = (hasHangul(meaning) ? meaning : (hasHangul(spelling) ? spelling : meaning || spelling));

                        return {
                            id,
                            spelling, meaning,
                            en: clean(en),
                            ko: clean(ko),
                            exampleSentence,
                            audioPath
                        };
                    }

                    let words = (Array.isArray(rawWords) ? rawWords : [])
                        .map(normalizeWord)
                        .filter(w => w && w.id != null && (w.en || w.ko || w.spelling || w.meaning));

                    if (!words.length) {
                        elPrompt.textContent = "오늘 학습할 단어가 없습니다.";
                        elNext.disabled = true;
                        return;
                    }

                    // 디버깅(원하면 확인 가능)
                    console.log("normalized sample", words.slice(0, 3));

                    const hasExample = (w) => !!clean(w.exampleSentence);
                    const hasAudio = (w) => !!clean(w.audioPath);

                    function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
                    function shuffle(arr) {
                        const a = arr.slice();
                        for (let i = a.length - 1; i > 0; i--) {
                            const j = Math.floor(Math.random() * (i + 1));
                            [a[i], a[j]] = [a[j], a[i]];
                        }
                        return a;
                    }

                    const finalTarget = (target > 0 ? target : words.length);

                    // 보기 풀(영/한)
                    const poolEn = Array.from(new Set(words.map(w => w.en).filter(Boolean)));
                    const poolKo = Array.from(new Set(words.map(w => w.ko).filter(Boolean)));

                    function buildOptionsFrom(pool, correctValue) {
                        const uniq = Array.from(new Set(pool.filter(Boolean)));
                        const others = uniq.filter(v => v !== correctValue);
                        const picks = shuffle(others).slice(0, 3);
                        const merged = Array.from(new Set([correctValue, ...picks]));
                        while (merged.length < 4) merged.push(pick(uniq));
                        return shuffle(merged).slice(0, 4);
                    }

                    // ✅ 규칙: "뜻:" 프롬프트가 한글이면 정답은 영어(en), 뜻이 영어이면 정답은 한글(ko)
                    function answerByMeaningPrompt(w, promptText) {
                        if (!promptText) return null;
                        return hasHangul(promptText) ? w.en : w.ko;
                    }

                    // ---------- 문제 생성기 ----------
                    function makeMCQ_WordToMeaning(w) {
                        if (!w.en || !w.ko || poolKo.length < 2) return null;
                        return {
                            type: "MCQ_MEANING",
                            wordId: w.id,
                            prompt: `단어: \${w.en}`,
                            sub: "",
                            answer: w.ko,
                            options: buildOptionsFrom(poolKo, w.ko)
                        };
                    }

                    function makeMCQ_MeaningToAnswer(w) {
                        // 뜻 프롬프트는 ko 우선(없으면 meaning/spelling)
                        const meaningPrompt = w.ko || w.meaning || "";
                        if (!meaningPrompt) return null;

                        const ans = answerByMeaningPrompt(w, meaningPrompt);
                        if (!ans) return null;

                        // 정답이 영어면 영어 보기, 정답이 한글이면 한글 보기
                        const isAnsKo = hasHangul(ans);
                        const pool = isAnsKo ? poolKo : poolEn;
                        if (pool.length < 2) return null;

                        return {
                            type: "MCQ_SPELLING",
                            wordId: w.id,
                            prompt: `뜻: \${meaningPrompt}`,
                            sub: "",
                            answer: ans,
                            options: buildOptionsFrom(pool, ans)
                        };
                    }

                    function makeSpellingInput(w) {
                        const meaningPrompt = w.ko || w.meaning || "";
                        if (!meaningPrompt) return null;
                        const ans = answerByMeaningPrompt(w, meaningPrompt);
                        if (!ans) return null;

                        return {
                            type: "SPELLING_INPUT",
                            wordId: w.id,
                            prompt: `뜻: \${meaningPrompt}`,
                            sub: "정답을 입력하세요",
                            answer: ans,
                            inputType: true
                        };
                    }

                    function makeScramble(w) {
                        const meaningPrompt = w.ko || w.meaning || "";
                        if (!meaningPrompt) return null;

                        // 스크램블은 "영어 답"일 때만 출제(한글 스크램블은 UX가 별로라 제외)
                        const ans = answerByMeaningPrompt(w, meaningPrompt);
                        if (!ans || hasHangul(ans) || ans.length < 4) return null;

                        const chars = ans.split("");
                        let scrambled = shuffle(chars).join("");
                        for (let k = 0; k < 8 && scrambled === ans; k++) scrambled = shuffle(chars).join("");

                        return {
                            type: "SCRAMBLE",
                            wordId: w.id,
                            prompt: `뜻: \${meaningPrompt}`,
                            sub: `섞인 단어: \${scrambled}`,
                            answer: ans,
                            inputType: true
                        };
                    }

                    function makeCloze(w) {
                        // CLOZE는 영어 예문 기반 → 정답은 영어(en)만
                        if (!w.en || !hasExample(w)) return null;

                        const s = String(w.exampleSentence);
                        const blanked = s.replace(new RegExp(w.en, "ig"), "_____");

                        return {
                            type: "CLOZE",
                            wordId: w.id,
                            prompt: "빈칸에 들어갈 단어는?",
                            sub: blanked,
                            answer: w.en,
                            options: buildOptionsFrom(poolEn, w.en)
                        };
                    }

                    function makeListen(w) {
                        if (!w.en || !hasAudio(w) || poolEn.length < 2) return null;
                        return {
                            type: "LISTEN",
                            wordId: w.id,
                            prompt: "음성을 듣고 단어를 고르세요",
                            sub: "",
                            answer: w.en,
                            audioPath: w.audioPath,
                            options: buildOptionsFrom(poolEn, w.en)
                        };
                    }

                    function makeMatch() {
                        const pool = words.filter(w => w.en && w.ko);
                        if (pool.length < 4) return null;

                        const picked = shuffle(pool).slice(0, 4);
                        const left = picked.map(w => ({ id: w.id, text: w.en }));
                        const right = shuffle(picked.map(w => ({ id: w.id, text: w.ko })));

                        return {
                            type: "MATCH",
                            prompt: "단어와 뜻을 짝지으세요",
                            sub: "4쌍 모두 맞춰야 완료됩니다.",
                            left, right
                        };
                    }

                    // ---------- 문제 리스트 생성 (LISTEN 최소 1문제 강제) ----------
                    const questions = [];
                    const audioWords = words.filter(w => w.en && hasAudio(w));

                    if (audioWords.length > 0) {
                        const lq = makeListen(pick(audioWords));
                        if (lq) questions.push(lq);
                    }

                    let guard = 0;
                    while (questions.length < finalTarget && guard < finalTarget * 40) {
                        guard++;

                        // MATCH 20%
                        if (Math.random() < 0.2) {
                            const mq = makeMatch();
                            if (mq) { questions.push(mq); continue; }
                        }

                        const w = pick(words);

                        const candidates = [];
                        const q1 = makeMCQ_WordToMeaning(w);
                        const q2 = makeMCQ_MeaningToAnswer(w);
                        const q3 = makeSpellingInput(w);
                        const q4 = makeScramble(w);
                        const q5 = makeCloze(w);
                        const q6 = makeListen(w);

                        if (q1) candidates.push(q1);
                        if (q2) candidates.push(q2);
                        if (q3) candidates.push(q3);
                        if (q4) candidates.push(q4);
                        if (q5) candidates.push(q5);
                        if (q6) candidates.push(q6);

                        if (!candidates.length) continue;
                        questions.push(pick(candidates));
                    }

                    const finalQuestions = questions.slice(0, finalTarget);

                    console.log("QUIZ types:", finalQuestions.reduce((m, q) => (m[q.type] = (m[q.type] || 0) + 1, m), {}));

                    // ---------- 서버 저장 ----------
                    async function saveAnswer(wordId, type, isCorrect) {
                        try {
                            const formData = new URLSearchParams();
                            formData.append("wordId", wordId);
                            formData.append("correct", isCorrect);

                            const res = await fetch(`${base}/learning/quiz/result`, {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/x-www-form-urlencoded",
                                    "X-Requested-With": "XMLHttpRequest"
                                },
                                body: formData.toString()
                            });

                            if (!res.ok) return;

                            let data = null;
                            try { data = await res.json(); } catch (e) { return; }

                            if (data?.resultCode?.startsWith("S-") && data.data) {
                                solved = data.data.quizSolvedCount;
                                remaining = data.data.quizRemainingCount;
                                correct = data.data.quizCorrectCount;
                                updateStats();
                            }
                        } catch (e) {
                            console.error("저장 실패", e);
                        }
                    }

                    function updateStats() {
                        elSolved.textContent = solved;
                        elCorrect.textContent = correct;
                        elRemaining.textContent = remaining;

                        const safeIdx = Math.min(idx + 1, finalTarget);
                        elProgressText.textContent = `${safeIdx} / ${finalTarget}`;
                    }

                    function showDone() {
                        cardBox.style.display = "none";
                        doneBox.style.display = "block";
                        doneCorrect.textContent = correct;
                        doneTotal.textContent = finalTarget;
                    }

                    function showFeedback(q, isCorrect) {
                        if (q.type === "MATCH") return;

                        const w = words.find(x => x.id === q.wordId);
                        if (!w) return;

                        let msg = "";
                        if (isCorrect) {
                            msg = "<span style='color:#2e7d32; font-size:1.1em;'>정답입니다!</span>";
                            elFeedback.style.background = "#e8f6ec";
                            elFeedback.style.border = "1px solid #a5d6a7";
                        } else {
                            msg = "<span style='color:#c62828; font-size:1.1em;'>틀렸습니다.</span><br>" +
                                "정답: <b>" + (w.en || w.spelling || "(단어없음)") + "</b> (" + (w.ko || w.meaning || "(뜻없음)") + ")";
                            elFeedback.style.background = "#ffecec";
                            elFeedback.style.border = "1px solid #ef9a9a";
                        }

                        if (w.exampleSentence) {
                            msg += "<div style='margin-top:8px; font-size:0.95em; color:#555; background:#fff; padding:8px; border-radius:6px; border:1px solid #eee;'>" +
                                "<b>예문:</b> " + w.exampleSentence + "</div>";
                        }

                        elFeedback.innerHTML = msg;
                        elFeedback.style.padding = "12px";
                        elFeedback.style.marginTop = "12px";
                        elFeedback.style.borderRadius = "8px";
                    }

                    // ---------- 렌더 ----------
                    let idx = Math.min(solved, finalQuestions.length - 1);
                    let locked = false;

                    function resetView() {
                        locked = false;
                        elOptions.innerHTML = "";
                        elMatchArea.innerHTML = "";
                        elInputArea.innerHTML = "";
                        elMatchArea.style.display = "none";
                        elInputArea.style.display = "none";
                        elOptions.style.display = "none";

                        elFeedback.textContent = "";
                        elFeedback.style.background = "transparent";
                        elFeedback.style.border = "none";
                        elFeedback.style.padding = "0";

                        elNext.disabled = true;

                        const oldAudioBtn = document.getElementById("customAudioBtn");
                        if (oldAudioBtn) oldAudioBtn.remove();
                    }

                    function render() {
                        resetView();

                        if (idx >= finalQuestions.length || remaining <= 0) {
                            showDone();
                            return;
                        }

                        const q = finalQuestions[idx];
                        updateStats();

                        elType.textContent = q.type;
                        elPrompt.textContent = q.prompt || "(문제 프롬프트 없음)";
                        elSub.textContent = q.sub || "";

                        // LISTEN UI
                        if (q.type === "LISTEN" && q.audioPath) {
                            const btn = document.createElement("button");
                            btn.id = "customAudioBtn";
                            btn.textContent = "▶ 듣기";
                            btn.style.marginBottom = "12px";
                            btn.style.background = "#ff9800";
                            btn.style.color = "white";
                            btn.style.border = "none";

                            btn.onclick = () => {
                                const audio = new Audio(q.audioPath);
                                audio.play().catch(() => alert("브라우저 정책으로 재생이 막힐 수 있어요. 다시 눌러주세요."));
                            };

                            elPrompt.parentNode.insertBefore(btn, elSub);
                        }

                        if (q.type === "MATCH") renderMatch(q);
                        else if (q.inputType) renderInput(q);
                        else renderMcq(q);
                    }

                    function renderMcq(q) {
                        elOptions.style.display = "flex";

                        q.options.forEach(opt => {
                            const btn = document.createElement("button");
                            btn.className = "option-btn";
                            btn.textContent = opt;

                            btn.onclick = async () => {
                                if (locked) return;
                                locked = true;

                                const isCorrect = (opt === q.answer);
                                btn.classList.add(isCorrect ? "correct" : "wrong");

                                if (!isCorrect) {
                                    Array.from(elOptions.children).forEach(child => {
                                        if (child.textContent === q.answer) child.classList.add("correct");
                                    });
                                }

                                showFeedback(q, isCorrect);
                                await saveAnswer(q.wordId, q.type, isCorrect);
                                elNext.disabled = false;
                            };

                            elOptions.appendChild(btn);
                        });
                    }

                    function renderInput(q) {
                        elInputArea.style.display = "block";

                        const wrap = document.createElement("div");
                        wrap.className = "input-area";
                        wrap.style.width = "100%";

                        const input = document.createElement("input");
                        input.className = "answer-input";
                        input.type = "text";
                        input.placeholder = "정답 입력";
                        input.autocomplete = "off";

                        const submit = document.createElement("button");
                        submit.className = "submit-btn";
                        submit.textContent = "제출";

                        async function doSubmit() {
                            if (locked) return;
                            const val = input.value.trim();
                            if (!val) return;

                            locked = true;
                            input.disabled = true;
                            submit.disabled = true;

                            const isCorrect = (val.toLowerCase() === String(q.answer).toLowerCase());
                            showFeedback(q, isCorrect);
                            await saveAnswer(q.wordId, q.type, isCorrect);
                            elNext.disabled = false;
                        }

                        submit.onclick = doSubmit;
                        input.onkeydown = (e) => { if (e.key === "Enter") doSubmit(); };

                        wrap.appendChild(input);
                        wrap.appendChild(submit);
                        elInputArea.appendChild(wrap);
                        input.focus();
                    }

                    // ✅ MATCH: 4쌍 모두 맞춰야 완료 + 1쌍 맞춰도 계속 선택 가능
                    function renderMatch(q) {
                        elMatchArea.style.display = "block";

                        const state = { leftIndex: null, matchedL: new Set(), matchedR: new Set() };

                        const wrap = document.createElement("div");
                        wrap.className = "match-wrap";

                        const leftCol = document.createElement("div");
                        leftCol.className = "match-col";
                        const rightCol = document.createElement("div");
                        rightCol.className = "match-col";

                        const leftEls = [];
                        const rightEls = [];

                        function clearLeftSelected() {
                            leftEls.forEach((el, idx) => {
                                if (!state.matchedL.has(idx)) el.classList.remove("selected");
                            });
                        }

                        q.left.forEach((item, i) => {
                            const el = document.createElement("div");
                            el.className = "match-item";
                            el.textContent = item.text;
                            el.onclick = () => {
                                if (state.matchedL.has(i)) return;
                                state.leftIndex = i;

                                leftEls.forEach((x, idx) => {
                                    if (state.matchedL.has(idx)) return;
                                    x.classList.toggle("selected", idx === i);
                                });
                            };
                            leftCol.appendChild(el);
                            leftEls.push(el);
                        });

                        q.right.forEach((item, j) => {
                            const el = document.createElement("div");
                            el.className = "match-item";
                            el.textContent = item.text;

                            el.onclick = async () => {
                                if (state.matchedR.has(j)) return;
                                if (state.leftIndex === null) return;

                                const li = state.leftIndex;
                                const ok = (q.left[li].id === q.right[j].id);

                                if (ok) {
                                    state.matchedL.add(li);
                                    state.matchedR.add(j);

                                    leftEls[li].classList.remove("selected");
                                    leftEls[li].classList.add("matched");
                                    rightEls[j].classList.add("matched");

                                    // ✅ 다음 선택 가능하게 즉시 초기화 (이게 핵심)
                                    state.leftIndex = null;
                                    clearLeftSelected();

                                    if (state.matchedL.size === 4 && state.matchedR.size === 4) {
                                        elFeedback.innerHTML = "<span style='color:#2e7d32; font-weight:bold;'>4쌍 모두 맞췄습니다!</span>";
                                        elFeedback.style.padding = "12px";
                                        elFeedback.style.background = "#e8f6ec";
                                        elFeedback.style.border = "1px solid #a5d6a7";

                                        await saveAnswer(q.left[0].id, "MATCH", true);
                                        elNext.disabled = false;
                                    }
                                } else {
                                    leftEls[li].classList.add("wrongflash");
                                    rightEls[j].classList.add("wrongflash");

                                    setTimeout(() => {
                                        leftEls[li].classList.remove("wrongflash");
                                        rightEls[j].classList.remove("wrongflash");
                                        state.leftIndex = null;
                                        clearLeftSelected();
                                    }, 450);
                                }
                            };

                            rightCol.appendChild(el);
                            rightEls.push(el);
                        });

                        wrap.appendChild(leftCol);
                        wrap.appendChild(rightCol);
                        elMatchArea.appendChild(wrap);
                    }

                    elNext.onclick = () => { idx++; render(); };

                    if (remaining <= 0) showDone();
                    else render();
                })();
            </script>

        </body>

        </html>