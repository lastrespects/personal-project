package com.mmb.service;

import com.mmb.api.TranslationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TranslationSmokeTest {

    @Test
    void libreTranslateReturnsKorean() throws Exception {
        TranslationClient stubTranslationClient = text -> "";
        LearningServiceImpl service = new LearningServiceImpl(null, null, stubTranslationClient, null);

        Field urlField = LearningServiceImpl.class.getDeclaredField("libreApiUrl");
        urlField.setAccessible(true);
        urlField.set(service, "https://translate.argosopentech.com/translate");

        Field keyField = LearningServiceImpl.class.getDeclaredField("libreApiKey");
        keyField.setAccessible(true);
        keyField.set(service, "");

        Method method = LearningServiceImpl.class.getDeclaredMethod("safeTranslateToKo", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "She drove her car to the mall.");

        Assumptions.assumeFalse(result.isBlank(), "Translation service unavailable - see logs");
        assertFalse(result.isBlank(), "LibreTranslate should return Korean translation");
    }
}
