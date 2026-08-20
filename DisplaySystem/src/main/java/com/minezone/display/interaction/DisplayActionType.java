package com.minezone.display.interaction;

import java.util.Locale;

public enum DisplayActionType {
    COMMAND,
    RANKING,
    GUI,
    TELEPORT,
    SERVER,
    NONE;

    public static DisplayActionType from(String value) {
        if (value == null || value.isBlank()) return NONE;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
