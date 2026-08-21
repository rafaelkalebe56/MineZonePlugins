package com.minezone.display.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtil {
    private static final LegacyComponentSerializer AMP = LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {}

    public static Component component(String text) {
        return AMP.deserialize(text == null ? "" : text);
    }

    public static Component color(String text) {
        return component(text);
    }
}
