# My Memory Booster - Server Setup

The project is a Spring Boot 3.5.x application that uses Maven, MySQL, JSP views, WebSockets, and a collection of REST clients (DictionaryAPI, DeepL, Gemini). This document summarizes what you need to prepare before starting the server locally.

## Prerequisites

- JDK 17 installed and available on your shell (`java -version` should show 17).
- Maven Wrapper already included in the repo; no need for a global Maven installation.
- Local MySQL (tested with XAMPP/MySQL 8). Create a schema named `MMB_DB` and grant access to the configured user (defaults are `root` with an empty password).
- Optional external APIs:
  - DeepL API key (`deepl.api.key` in `application.properties`).
  - Gemini API key (`GEMINI_API_KEY` env var or `gemini.api.key` property) for AI content generation fallbacks.
  - Gmail SMTP credentials (`GMAIL_USERNAME`, `GMAIL_APP_PASSWORD`) if you want to test the mailer.

## Configuration Checklist

1. **Database** – adjust `spring.datasource.*` in `src/main/resources/application.properties` to match your local DB credentials.
2. **Uploads** – ensure `custom.file.dir` exists (default: `d:/mwg/workspace/mmb/uploads`) or update it to a writable path.
3. **API Keys** – set the DeepL/Gemini/Gmail credentials as environment variables or override them via `application-local.properties`.
4. **Port** – server defaults to `8081` (`server.port`). Change if the port is already in use.

## Running the Server

```powershell
# from the project root
./mvnw clean package
./mvnw spring-boot:run
```

> The `clean package` step ensures `target/` classes are regenerated with the merged learning service logic. You can skip it for faster iterations once dependencies are downloaded.

The application exposes REST + JSP endpoints:

- `http://localhost:8081/view/usr/home/main.jsp` – JSP home.
- `/api/learning/today?memberId=1` – REST endpoint returning today’s quiz words (delegates to the updated `LearningService`/`FullLearningService`).

## Verifying the Setup

1. Start MySQL and confirm `MMB_DB` is reachable.
2. Run `./mvnw spring-boot:run` and wait for the “Started MmbApplication” log.
3. Hit `/actuator/health` or `/api/learning/today` with a valid `memberId` to verify the merged learning workflow works end-to-end.
4. Tail `logs/app.log` if you need verbose security/logging output (log path configured in `application.properties`).\

With these items prepared the “서버 세팅” step is complete, and you can proceed with front-end/JSP work or API/Learning feature tweaks.*** End Patch
