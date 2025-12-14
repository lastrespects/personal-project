$ErrorActionPreference = "Continue"

# [0] 프로젝트 루트로 이동 (안 맞으면 너 프로젝트 경로로 바꿔)
cd D:\mwg\workspace\mmb

# [1] src/main/java 존재 확인 (없으면 여기서 바로 경로 문제임)
Get-ChildItem .\src -Recurse -Directory | Select-Object -First 20

# [2] 자바 파일이 실제로 몇 개인지 확인
(Get-ChildItem .\src\main\java -Recurse -Filter *.java -ErrorAction SilentlyContinue).Count

# [3] 먼저 "learning/quiz/word" 관련 파일명 자체를 찾아서 경로를 확정
Get-ChildItem .\src\main\java -Recurse -Filter *.java |
  Where-Object { $_.FullName -match "learn|quiz|word|study|target|progress|record" } |
  Select-Object -First 50 FullName

# [4] 이제 "무조건 걸리는" 넓은 키워드로 본문 검색 (여기서 무조건 뭐라도 나와야 정상)
$patterns = @(
  "dailyTarget",
  "DAILY_WORD_COUNT",
  "daily.word.count",
  "server.port",
  "Quiz",
  "quiz",
  "Learning",
  "learning",
  "Word",
  "word",
  "StudyRecord",
  "studyRecord",
  "WordProgress",
  "wordProgress",
  "PageRequest",
  "limit",
  "setMaxResults",
  "getDailyTarget"
)

# [5] 전부 grep해서 파일로 저장 (한 줄도 안 나오면 src/main/java 자체를 못 읽는 상태)
Remove-Item .\_debug_daily_target_grep.txt -ErrorAction SilentlyContinue

foreach ($p in $patterns) {
  "===== PATTERN: $p =====" | Out-File -Append -Encoding utf8 .\_debug_daily_target_grep.txt
  Select-String -Path .\src\main\java\**\*.java -Pattern $p -ErrorAction SilentlyContinue |
    ForEach-Object { $_.Path + ":" + $_.LineNumber + " : " + $_.Line } |
    Out-File -Append -Encoding utf8 .\_debug_daily_target_grep.txt
  "" | Out-File -Append -Encoding utf8 .\_debug_daily_target_grep.txt
}

# [6] 결과 파일이 비었는지 체크
(Get-Item .\_debug_daily_target_grep.txt).Length
type .\_debug_daily_target_grep.txt
