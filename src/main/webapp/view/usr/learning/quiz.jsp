<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/mmb.css">
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>오늘의 퀴즈 - My Memory Book</title>

  <!-- Tailwind CDN -->
  <script src="https://cdn.tailwindcss.com"></script>

  <!-- ✅ 디자인 전용 CSS (로직 영향 없음) -->
  <style>
    :root{
      --bg: #f9fafb;
      --card: #ffffff;
      --bd: #e5e7eb;
      --text: #111827;
      --muted: #6b7280;

      --indigo: #4f46e5;
      --indigo-weak: #eef2ff;

      --ok: #22c55e;
      --ok-weak: #f0fdf4;

      --bad: #ef4444;
      --bad-weak: #fef2f2;

      --shadow: 0 1px 2px rgba(0,0,0,.05);
      --r-xl: 16px;
      --r-lg: 12px;
    }

    body{ background: var(--bg); color: var(--text); }

    /* 카드 */
    .card{
      background: var(--card);
      border: 1px solid var(--bd);
      border-radius: var(--r-xl);
      padding: 24px;
      box-shadow: var(--shadow);
    }

    /* 타이포 */
    .title{ font-size: 24px; font-weight: 800; letter-spacing: -0.02em; }
    .caption{ font-size: 14px; color: var(--muted); }

    /* 배지 */
    .badge{
      font-size: 12px;
      padding: 6px 12px;
      border-radius: 999px;
      border: 1px solid var(--bd);
      background: var(--indigo-weak);
      color: #4338ca;
      font-weight: 700;
      white-space: nowrap;
    }

    /* 상단 상태 카드 */
    .stat{
      border: 1px solid var(--bd);
      border-radius: 14px;
      padding: 12px;
      background: #fff;
    }
    .stat .k{ font-size: 12px; color: var(--muted); }
    .stat .v{ font-size: 18px; font-weight: 800; margin-top: 2px; }

    /* 버튼 */
    .btn-primary{
      padding: 10px 16px;
      border-radius: 14px;
      background: var(--indigo);
      color: #fff;
      font-weight: 700;
      transition: opacity .15s ease, transform .15s ease;
    }
    .btn-primary:hover{ opacity: .95; transform: translateY(-1px); }
    .btn-primary:disabled{ opacity: .4; cursor: not-allowed; transform: none; }

    .btn-ghost{
      padding: 10px 16px;
      border-radius: 14px;
      border: 1px solid var(--bd);
      background: #fff;
      font-weight: 700;
      transition: background .15s ease, transform .15s ease;
    }
    .btn-ghost:hover{ background: #f9fafb; transform: translateY(-1px); }

    /* 선택지 버튼 */
    .opt-btn{
      width: 100%;
      text-align: left;
      padding: 12px 16px;
      border-radius: 14px;
      border: 1px solid var(--bd);
      background: #fff;
      font-weight: 600;
      transition: background .15s ease, transform .15s ease, border-color .15s ease;
    }
    .opt-btn:hover{ background: #f9fafb; transform: translateY(-1px); }
    .opt-btn:disabled{ opacity: .9; cursor: not-allowed; transform: none; }

    .opt-correct{ border-color: var(--ok) !important; background: var(--ok-weak) !important; }
    .opt-wrong{ border-color: var(--bad) !important; background: var(--bad-weak) !important; }

    /* 진행바 */
    .progress-track{
      height: 8px;
      width: 100%;
      border-radius: 999px;
      background: #f3f4f6;
      overflow: hidden;
    }
    .progress-fill{
      height: 8px;
      border-radius: 999px;
      background: var(--indigo);
      width: 0%;
    }

    /* 입력 */
    .inp{
      width: 100%;
      padding: 12px 16px;
      border-radius: 14px;
      border: 1px solid var(--bd);
      outline: none;
    }
    .inp:focus{ box-shadow: 0 0 0 4px rgba(79,70,229,.15); border-color: rgba(79,70,229,.35); }

    /* 매칭 박스 */
    .match-box{
      border: 1px solid var(--bd);
      border-radius: 14px;
      padding: 12px;
      background: #fff;
    }
    .match-title{
      font-size: 12px;
      color: var(--muted);
      font-weight: 700;
      margin-bottom: 8px;
    }
    .match-item{
      width: 100%;
      text-align: left;
      padding: 10px 12px;
      border-radius: 12px;
      border: 1px solid var(--bd);
      background: #fff;
      margin-bottom: 8px;
      font-weight: 600;
      transition: background .15s ease, transform .15s ease;
    }
    .match-item:hover{ background:#f9fafb; transform: translateY(-1px); }
    .match-item:disabled{ opacity:.55; cursor:not-allowed; transform:none; }

    /* 작은 토스트 느낌 피드백 */
    .feedback{
      padding: 10px 12px;
      border-radius: 14px;
      border: 1px solid var(--bd);
      background: #fff;
      display: inline-block;
    }
  </style>
</head>

<body class="bg-gray-50">
    <%@ include file="/view/usr/common/header.jsp" %>
<section class="py-8">
  <div class="container mx-auto max-w-4xl px-4">

    <!-- 상단 카드 -->
    <div class="card">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h1 class="title">오늘의 퀴즈</h1>
          <p class="caption mt-1">오늘 학습 단어를 문제로 풀어보세요.</p>
        </div>
        <div class="flex items-center gap-2">
          <a href="/usr/home/main" class="btn-ghost text-sm">메인</a>
        </div>
      </div>

      <!-- 상태 카드 -->
      <div class="mt-5 grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="stat">
          <div class="k">오늘 목표</div>
          <div class="v"><span id="totalWordsText"><c:out value="${dailyTarget}" /></span>개</div>
        </div>
        <div class="stat">
          <div class="k">남은 단어</div>
          <div class="v"><span id="remainingCount"><c:out value="${quizRemainingCount}" /></span>개</div>
        </div>
        <div class="stat">
          <div class="k">정답</div>
          <div class="v"><span id="correctCount"><c:out value="${quizCorrectCount}" /></span></div>
        </div>
        <div class="stat">
          <div class="k">푼 단어</div>
          <div class="v"><span id="solvedCount"><c:out value="${quizSolvedCount}" /></span></div>
        </div>
      </div>

      <!-- 진행바 -->
      <div class="mt-4">
        <div class="flex items-center justify-between text-xs text-gray-500 mb-2">
          <span>진행(단어): <b><span id="solvedNowText">0</span></b> / <b><span id="solvedTotalText">0</span></b></span>
          <span id="stepHintText"></span>
        </div>
        <div class="progress-track">
          <div id="progressBar" class="progress-fill"></div>
        </div>
      </div>
    </div>

    <!-- 문제 카드 -->
    <div class="card mt-6">
      <div class="flex items-center justify-between gap-3">
        <div class="caption">문제</div>
        <div id="questionTypeBadge" class="badge">-</div>
      </div>

      <div class="mt-4" id="questionArea"></div>

      <div class="mt-5 flex flex-wrap items-center gap-2">
        <button id="nextBtn" class="btn-primary" disabled>다음 문제</button>
        <button id="retryBtn" class="btn-ghost hidden">다시하기</button>
      </div>

      <div class="mt-4 text-sm" id="feedbackText"></div>
    </div>

    <!-- ✅ 서버에서 내려준 JSON을 JS 객체로 바로 주입 -->
    <script>
      window.__RAW_WORDS__ = ${wordsJson};
    </script>

    <script>
      (function(){

        // ===============================
        // 듣기(Wordbook과 동일)
        // ===============================
        function fallbackSpeak(text) {
          if (!window.speechSynthesis) {
            alert('브라우저에서 음성 출력이 지원되지 않습니다.');
            return;
          }
          var utter = new SpeechSynthesisUtterance(text);
          utter.lang = 'en-US';
          speechSynthesis.cancel();
          speechSynthesis.speak(utter);
        }

        function playAudioByWord(word){
          var spelling = word.spelling;
          var audioPath = word.audioPath;

          if (audioPath && String(audioPath).trim().length > 0) {
            var audio = new Audio(audioPath);
            audio.play().catch(function(){ fallbackSpeak(spelling); });
          } else {
            fallbackSpeak(spelling);
          }
        }

        // ===============================
        // util
        // ===============================
        function toInt(v, fallback){
          var n = Number(v);
          return Number.isFinite(n) ? n : (fallback == null ? 0 : fallback);
        }
        function normStr(v){ return (v == null) ? "" : String(v).trim(); }
        function shuffle(arr){
          for (var i=arr.length-1; i>0; i--){
            var j = Math.floor(Math.random() * (i+1));
            var t = arr[i]; arr[i]=arr[j]; arr[j]=t;
          }
          return arr;
        }
        function uniqueById(list){
          var m = new Map();
          for (var i=0;i<list.length;i++){
            var w=list[i];
            if (w.id && !m.has(w.id)) m.set(w.id,w);
          }
          return Array.from(m.values());
        }
        function escapeRegExp(str){
          // ✅ JSP가 오해할만한 문자열(빈 EL 등) 안 만들고, 정규식 메타문자만 안전하게 escape
          return String(str).replace(/[.*+?^$\\{}()|[\]\\]/g, "\\$&");
        }

        // ===============================
        // data normalize (서버 주입)
        // ===============================
        var raw = Array.isArray(window.__RAW_WORDS__) ? window.__RAW_WORDS__ : [];
        var words = raw.map(function(w){
          return {
            id: toInt(w.id, 0),
            spelling: normStr(w.spelling),
            meaning: normStr(w.meaning),
            exampleSentence: normStr(w.exampleSentence),
            audioPath: normStr(w.audioPath)
          };
        }).filter(function(w){
          return w.id > 0 && w.spelling && w.meaning;
        });

        words = uniqueById(words);

        // ===============================
        // DOM
        // ===============================
        var elTotalWordsText = document.getElementById("totalWordsText");
        var elRemaining = document.getElementById("remainingCount");
        var elCorrect = document.getElementById("correctCount");
        var elSolved  = document.getElementById("solvedCount");
        var elSolvedNowText = document.getElementById("solvedNowText");
        var elSolvedTotalText = document.getElementById("solvedTotalText");
        var elProgressBar = document.getElementById("progressBar");
        var elStepHint = document.getElementById("stepHintText");

        var elBadge = document.getElementById("questionTypeBadge");
        var elQuestionArea = document.getElementById("questionArea");
        var elNext = document.getElementById("nextBtn");
        var elRetry = document.getElementById("retryBtn");
        var elFeedback = document.getElementById("feedbackText");

        // ===============================
        // counts init (목표 기준 진행률)
        // ===============================
        var target = toInt(elTotalWordsText.textContent, words.length);
        elSolvedTotalText.textContent = String(target);

        function updateTopUI(){
          var solved = toInt(elSolved.textContent, 0);
          elSolvedNowText.textContent = String(Math.min(solved, target));
          var pct = (target === 0) ? 0 : Math.round((solved / target) * 100);
          elProgressBar.style.width = pct + "%";
        }

        // ===============================
        // server save (세션 기반)
        // ✅ /learning/quiz/result 로 통일 (기존 그대로 유지)
        // ===============================
        function saveResult(wordId, correct){
          return fetch("/learning/quiz/result", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: "wordId=" + encodeURIComponent(wordId) + "&correct=" + encodeURIComponent(!!correct)
          }).then(function(res){ return res.json(); })
            .then(function(json){
              var data = json && (json.rsData || json.data || {});
              if (data.quizRemainingCount != null) elRemaining.textContent = String(toInt(data.quizRemainingCount, toInt(elRemaining.textContent, 0)));
              if (data.quizCorrectCount != null) elCorrect.textContent = String(toInt(data.quizCorrectCount, toInt(elCorrect.textContent, 0)));
              if (data.quizSolvedCount != null)  elSolved.textContent  = String(toInt(data.quizSolvedCount,  toInt(elSolved.textContent, 0)));
              updateTopUI();
            });
        }

        function saveBatch(results){
          var p = Promise.resolve();
          results.forEach(function(r){
            p = p.then(function(){ return saveResult(r.wordId, r.correct); });
          });
          return p;
        }

        // ===============================
        // render helpers
        // ===============================
        function setBadge(text){ elBadge.textContent = text; }
        function setFeedback(msg, color){
          elFeedback.textContent = msg || "";
          elFeedback.className = "mt-4 text-sm " + (color ? color : "");
        }
        function disableNext(){ elNext.disabled = true; }
        function enableNext(){ elNext.disabled = false; }

        function optionButton(text){
          var b = document.createElement("button");
          b.type = "button";
          // ✅ 디자인 class만 적용 (로직 변화 없음)
          b.className = "opt-btn";
          b.textContent = text;
          return b;
        }

        // ===============================
        // words 없으면 종료
        // ===============================
        if (words.length === 0){
          setBadge("EMPTY");
          elQuestionArea.innerHTML = "<div class='text-gray-600 font-semibold'>오늘의 단어가 없습니다. 단어장부터 채워주세요.</div>";
          elNext.disabled = true;
          elRetry.classList.remove("hidden");
          updateTopUI();
          return;
        }

        // ===============================
        // step builder
        // ===============================
        var queue = shuffle(words.slice());
        var steps = [];
        var idx = 0;
        var ROT = ["MCQ_KO2EN", "MCQ_EN2KO", "CLOZE", "LISTEN"];

        while (idx < queue.length){
          if ((steps.length % 5 === 4) && (queue.length - idx >= 4)){
            steps.push({ kind:"MATCH", words: queue.slice(idx, idx+4) });
            idx += 4;
          } else {
            var w = queue[idx++];
            var t = ROT[steps.length % ROT.length];
            steps.push({ kind:"SINGLE", word: w, qtype: t });
          }
        }

        var stepPos = 0;
        var answered = false;
        var isSubmitting = false;

        // ===============================
        // 1) MCQ
        // ===============================
        function renderMcq(word, dir){
          var title = document.createElement("div");
          title.className = "caption";
          title.textContent = "4지선다";

          var prompt = document.createElement("div");
          prompt.className = "mt-2 text-xl font-extrabold tracking-tight";

          var correctText = "";
          var pool = words.filter(function(x){ return x.id !== word.id; });
          shuffle(pool);

          var choices = [];

          if (dir === "KO2EN"){
            setBadge("한글 → 영어 (4지선다)");
            prompt.textContent = word.meaning;
            correctText = word.spelling;
            choices = [word.spelling];
            for (var i=0; i<pool.length && choices.length<4; i++){
              var c = pool[i].spelling;
              if (choices.indexOf(c) === -1) choices.push(c);
            }
          } else {
            setBadge("영어 → 한글 (4지선다)");
            prompt.textContent = word.spelling;
            correctText = word.meaning;
            choices = [word.meaning];
            for (var j=0; j<pool.length && choices.length<4; j++){
              var m = pool[j].meaning;
              if (choices.indexOf(m) === -1) choices.push(m);
            }
          }
          shuffle(choices);

          var area = document.createElement("div");
          area.className = "mt-4 grid gap-3";

          choices.forEach(function(ch){
            var btn = optionButton(ch);
            btn.addEventListener("click", function(){
              if (answered || isSubmitting) return;
              answered = true;
              isSubmitting = true;
              disableNext();

              var ok = (ch === correctText);

              Array.from(area.querySelectorAll("button")).forEach(function(b){
                b.disabled = true;
              });

              if (ok){
                btn.classList.add("opt-correct");
                setFeedback("정답!", "text-green-600");
              } else {
                btn.classList.add("opt-wrong");
                setFeedback("오답! 정답: " + correctText, "text-red-600");
              }

              saveResult(word.id, ok)
                .catch(function(e){
                  console.error(e);
                  setFeedback("저장 오류가 발생했지만 다음 문제로 진행할 수 있습니다.", "text-orange-600");
                })
                .finally(function(){
                  isSubmitting = false;
                  enableNext();
                });
            });
            area.appendChild(btn);
          });

          elQuestionArea.innerHTML = "";
          elQuestionArea.appendChild(title);
          elQuestionArea.appendChild(prompt);
          elQuestionArea.appendChild(area);
        }

        // ===============================
        // 2) CLOZE
        // ===============================
        function renderCloze(word){
          setBadge("빈칸/혼합 → 단어 입력");

          var wrap = document.createElement("div");
          wrap.className = "mt-2";

          var title = document.createElement("div");
          title.className = "caption";
          title.textContent = "혼합된 글자를 보고 단어를 완성하세요.";

          var question = document.createElement("div");
          question.className = "mt-3 text-lg font-semibold";

          if (word.exampleSentence){
            var re = new RegExp("\\b" + escapeRegExp(word.spelling) + "\\b", "ig");
            question.textContent = word.exampleSentence.replace(re, "____");
          } else {
            question.textContent = "뜻: " + word.meaning;
          }

          var letters = word.spelling.split("");
          shuffle(letters);

          var chips = document.createElement("div");
          chips.className = "mt-3 flex flex-wrap gap-2";
          letters.forEach(function(ch){
            var s = document.createElement("span");
            s.className = "px-2 py-1 rounded-lg border bg-gray-50 text-sm font-mono";
            s.textContent = ch;
            chips.appendChild(s);
          });

          var row = document.createElement("div");
          row.className = "mt-4 flex flex-col sm:flex-row gap-2";

          var input = document.createElement("input");
          input.className = "inp flex-1";
          input.placeholder = "정답 단어 입력 (영어)";
          input.autocomplete = "off";

          var checkBtn = document.createElement("button");
          checkBtn.type = "button";
          checkBtn.className = "btn-primary";
          checkBtn.textContent = "확인";

          row.appendChild(input);
          row.appendChild(checkBtn);

          checkBtn.addEventListener("click", function(){
            if (answered || isSubmitting) return;
            answered = true;
            isSubmitting = true;
            disableNext();

            var ans = normStr(input.value).toLowerCase();
            var ok = (ans === word.spelling.toLowerCase());

            input.disabled = true;
            checkBtn.disabled = true;

            if (ok) setFeedback("정답!", "text-green-600");
            else setFeedback("오답! 정답: " + word.spelling, "text-red-600");

            saveResult(word.id, ok)
              .catch(function(e){
                console.error(e);
                setFeedback("저장 오류가 발생했지만 다음 문제로 진행할 수 있습니다.", "text-orange-600");
              })
              .finally(function(){
                isSubmitting = false;
                enableNext();
              });
          });

          wrap.appendChild(title);
          wrap.appendChild(question);
          wrap.appendChild(chips);
          wrap.appendChild(row);

          elQuestionArea.innerHTML = "";
          elQuestionArea.appendChild(wrap);
        }

        // ===============================
        // 3) LISTEN
        // ===============================
        function renderListen(word){
          setBadge("듣기 → 단어 고르기");

          var title = document.createElement("div");
          title.className = "caption mt-2";
          title.textContent = "버튼을 눌러 발음을 듣고 맞는 단어를 고르세요.";

          var playRow = document.createElement("div");
          playRow.className = "mt-3 flex flex-col sm:flex-row sm:items-center gap-2";

          var playBtn = document.createElement("button");
          playBtn.type = "button";
          playBtn.className = "btn-primary";
          playBtn.textContent = "🔊 듣기";
          playBtn.addEventListener("click", function(){ playAudioByWord(word); });

          var tip = document.createElement("div");
          tip.className = "text-xs text-gray-500";
          tip.textContent = "오디오 파일이 없으면 브라우저 음성으로 읽어줘요.";

          playRow.appendChild(playBtn);
          playRow.appendChild(tip);

          var pool = words.filter(function(x){ return x.id !== word.id; });
          shuffle(pool);
          var choices = [word.spelling];
          for (var i=0; i<pool.length && choices.length<4; i++){
            var s = pool[i].spelling;
            if (choices.indexOf(s) === -1) choices.push(s);
          }
          shuffle(choices);

          var area = document.createElement("div");
          area.className = "mt-4 grid gap-3";

          choices.forEach(function(ch){
            var btn = optionButton(ch);
            btn.addEventListener("click", function(){
              if (answered || isSubmitting) return;
              answered = true;
              isSubmitting = true;
              disableNext();

              var ok = (ch === word.spelling);

              Array.from(area.querySelectorAll("button")).forEach(function(b){ b.disabled = true; });

              if (ok){
                btn.classList.add("opt-correct");
                setFeedback("정답!", "text-green-600");
              } else {
                btn.classList.add("opt-wrong");
                setFeedback("오답! 정답: " + word.spelling, "text-red-600");
              }

              saveResult(word.id, ok)
                .catch(function(e){
                  console.error(e);
                  setFeedback("저장 오류가 발생했지만 다음 문제로 진행할 수 있습니다.", "text-orange-600");
                })
                .finally(function(){
                  isSubmitting = false;
                  enableNext();
                });
            });
            area.appendChild(btn);
          });

          elQuestionArea.innerHTML = "";
          elQuestionArea.appendChild(title);
          elQuestionArea.appendChild(playRow);
          elQuestionArea.appendChild(area);
        }

        // ===============================
        // 4) MATCH
        // ===============================
        function renderMatch(list4){
          setBadge("매칭 (4쌍)");

          var hint = document.createElement("div");
          hint.className = "caption mt-2";
          hint.textContent = "왼쪽(영어)과 오른쪽(한글)을 짝지으세요. (4쌍 완료하면 저장됩니다)";

          var grid = document.createElement("div");
          grid.className = "mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4";

          var left = document.createElement("div");
          left.className = "match-box";
          var right = document.createElement("div");
          right.className = "match-box";

          var leftTitle = document.createElement("div");
          leftTitle.className = "match-title";
          leftTitle.textContent = "영어";
          var rightTitle = document.createElement("div");
          rightTitle.className = "match-title";
          rightTitle.textContent = "한글";

          left.appendChild(leftTitle);
          right.appendChild(rightTitle);

          var L = list4.map(function(w){ return { id:w.id, text:w.spelling }; });
          var R = shuffle(list4.map(function(w){ return { id:w.id, text:w.meaning }; }));

          var selL = null;
          var selR = null;
          var pairs = [];
          var lockedL = new Set();
          var lockedR = new Set();

          function renderCols(){
            Array.from(left.querySelectorAll("button")).forEach(function(b){ b.remove(); });
            Array.from(right.querySelectorAll("button")).forEach(function(b){ b.remove(); });

            L.forEach(function(item){
              var b = document.createElement("button");
              b.type = "button";
              b.className = "match-item";
              b.textContent = item.text;

              if (lockedL.has(item.id)) { b.disabled = true; }
              if (selL && selL.id === item.id) { b.classList.add("ring-2","ring-indigo-300","bg-indigo-50"); }

              b.addEventListener("click", function(){
                if (lockedL.has(item.id)) return;
                selL = item;
                renderCols();
                tryPair();
              });
              left.appendChild(b);
            });

            R.forEach(function(item){
              var b = document.createElement("button");
              b.type = "button";
              b.className = "match-item";
              b.textContent = item.text;

              if (lockedR.has(item.id)) { b.disabled = true; }
              if (selR && selR.id === item.id) { b.classList.add("ring-2","ring-indigo-300","bg-indigo-50"); }

              b.addEventListener("click", function(){
                if (lockedR.has(item.id)) return;
                selR = item;
                renderCols();
                tryPair();
              });
              right.appendChild(b);
            });
          }

          function tryPair(){
            if (!selL || !selR) return;

            lockedL.add(selL.id);
            lockedR.add(selR.id);

            pairs.push({ wordId: selL.id, pickedMeaningId: selR.id });

            selL = null;
            selR = null;
            renderCols();

            if (pairs.length === 4){
              answered = true;
              isSubmitting = true;
              disableNext();

              var results = pairs.map(function(p){
                return { wordId: p.wordId, correct: (p.wordId === p.pickedMeaningId) };
              });

              var okCnt = results.filter(function(r){ return r.correct; }).length;
              setFeedback("매칭 완료! (정답 " + okCnt + " / 4) 저장 중...", "text-gray-600");

              saveBatch(results)
                .catch(function(e){
                  console.error(e);
                  setFeedback("저장 오류가 발생했지만 다음 문제로 진행할 수 있습니다.", "text-orange-600");
                })
                .finally(function(){
                  isSubmitting = false;
                  enableNext();
                  setFeedback("저장 완료! (정답 " + okCnt + " / 4)", okCnt === 4 ? "text-green-600" : "text-indigo-700");
                });
            }
          }

          renderCols();

          elQuestionArea.innerHTML = "";
          elQuestionArea.appendChild(hint);
          grid.appendChild(left);
          grid.appendChild(right);
          elQuestionArea.appendChild(grid);
        }

        // ===============================
        // load step
        // ===============================
        function loadStep(){
          answered = false;
          disableNext();
          setFeedback("", "");

          if (stepPos >= steps.length){
            setBadge("DONE");
            elQuestionArea.innerHTML = "<div class='text-xl font-extrabold'>오늘의 퀴즈가 끝났습니다! 🎉</div>";
            elNext.disabled = true;
            elRetry.classList.remove("hidden");
            elStepHint.textContent = "";
            return;
          }

          var step = steps[stepPos];
          elStepHint.textContent = (step.kind === "MATCH") ? "이번 문제는 4쌍 매칭입니다." : "";

          if (step.kind === "MATCH"){
            renderMatch(step.words);
          } else {
            var w = step.word;
            var t = step.qtype;
            if (t === "MCQ_KO2EN") renderMcq(w, "KO2EN");
            else if (t === "MCQ_EN2KO") renderMcq(w, "EN2KO");
            else if (t === "CLOZE") renderCloze(w);
            else if (t === "LISTEN") renderListen(w);
            else renderMcq(w, "KO2EN");
          }
        }

        elNext.addEventListener("click", function(){
          if (!answered) return;
          stepPos++;
          loadStep();
        });

        elRetry.addEventListener("click", function(){ location.reload(); });

        // init
        updateTopUI();
        loadStep();

      })();
    </script>

  </div>
</section>
<%@ include file="/view/usr/common/footer.jsp" %>
</body>
</html>
