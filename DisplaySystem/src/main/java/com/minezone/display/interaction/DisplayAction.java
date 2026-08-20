package com.minezone.display.interaction;

public record DisplayAction(DisplayActionType type, String value) {
    public DisplayAction {
        if (type == null) type = DisplayActionType.NONE;
        if (value == null) value = "";
    }
}
