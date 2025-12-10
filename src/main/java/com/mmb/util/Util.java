package com.mmb.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {

    public static String dateTimeToStr(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    
    // 이 메서드가 구문 오류의 핵심 원인이었습니다. 텍스트 블록을 일반 문자열로 수정합니다.
    public static String jsReplace(String msg, String uri) {

        if (msg == null) {
            msg = "";
        }
        
        if (uri == null || uri.length() == 0) {
            uri = "/";
        }
        
        // 텍스트 블록(triple quotes) 문법을 일반 문자열 결합으로 변경
        return "<script>"
               + "const msg = '" + msg.trim() + "';"
               + "if (msg.length > 0) {"
               + "    requestAnimationFrame(() => {"
               + "        alert(msg);"
               + "    });"
               + "}"
               + "const uri = '" + uri.trim() + "';"
               + "if (uri === 'hb') {"
               + "    history.back();"
               + "} else {"
               + "    setTimeout(() => {"
               + "        location.replace(uri);"
               + "    }, 100);"
               + "}"
               + "</script>";
    }
    
    public static String encryptSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 지원하지 않습니다.", e);
        }
    }
}