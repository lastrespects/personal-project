package com.mmb.util;

import java.util.Arrays;
import java.util.Optional;

public enum BoardType {
    NOTICE(1, "NOTICE", "공지사항"),
    QNA(2, "QNA", "질문답변");

    private final int id;
    private final String code;
    private final String displayName;

    BoardType(int id, String code, String displayName) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<BoardType> fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst();
    }
}
