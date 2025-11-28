package com.mmb.external.client;

import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * TTS 클라이언트(예시용).
 * - OpenAI TTS(예시 엔드포인트)로 요청하여 mp3 파일로 저장한다.
 * - 실제 엔드포인트/파라미터는 사용 API 문서에 따라 조정 필요.
 *
 * 안전: API 키는 환경변수 OPENAI_API_KEY로 읽습니다.
 */
@Component
public class TtsClient {

    private final OkHttpClient http = new OkHttpClient();
    private final String apiKey = System.getenv("OPENAI_API_KEY"); // 권장: env var

    private static final String TTS_URL = "https://api.openai.com/v1/audio/speech"; // 예시

    /**
     * text를 TTS로 변환하여 audio/{fileName}.mp3로 저장, 경로 반환.
     * 실패 시 null 반환.
     */
    public String synthesize(String text, String fileName) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY not set -> skipping TTS");
            return null;
        }

        try {
            MediaType json = MediaType.get("application/json; charset=utf-8");
            // 예시 body — 실제 스펙에 맞춰 조정 필요
            String bodyJson = "{ \"model\":\"gpt-4o-mini-tts\", \"voice\":\"alloy\", \"input\": \"" + escapeJson(text) + "\" }";
            RequestBody body = RequestBody.create(bodyJson, json);

            Request req = new Request.Builder()
                    .url(TTS_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    System.err.println("TTS request failed: " + resp);
                    return null;
                }

                File dir = new File("audio");
                if (!dir.exists()) dir.mkdirs();
                File out = new File(dir, fileName + ".mp3");

                try (InputStream in = resp.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) fos.write(buffer, 0, len);
                }
                return out.getPath();
            }
        } catch (Exception e) {
            System.err.println("TTS synth error: " + e.getMessage());
            return null;
        }
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
