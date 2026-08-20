package com.minezone.display.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public final class FormatUtil {
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final ThreadLocal<DecimalFormat> MONEY = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(PT_BR);
        DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
        format.setGroupingUsed(true);
        return format;
    });
    private static final ThreadLocal<NumberFormat> INTEGER = ThreadLocal.withInitial(() -> NumberFormat.getIntegerInstance(PT_BR));

    private FormatUtil() {}

    public static String money(double value) {
        return "$" + MONEY.get().format(Math.max(0D, value));
    }

    public static String integer(long value) {
        return INTEGER.get().format(value);
    }

    public static String duration(long millis) {
        long totalMinutes = Math.max(0L, millis) / 60_000L;
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        if (hours <= 0L && minutes <= 0L) return "<1m";
        if (hours <= 0L) return minutes + "m";
        return hours + "h " + minutes + "m";
    }
}
