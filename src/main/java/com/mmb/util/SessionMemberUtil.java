package com.mmb.util;

import jakarta.servlet.http.HttpSession;

public final class SessionMemberUtil {

    private SessionMemberUtil() {
    }

    public static Integer getSessionMemberId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object v = session.getAttribute("loginedMemberId");
        if (v == null) {
            return null;
        }
        if (v instanceof Integer i) {
            return i;
        }
        if (v instanceof Long l) {
            return l.intValue();
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
