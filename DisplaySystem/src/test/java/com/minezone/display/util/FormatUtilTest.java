package com.minezone.display.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatUtilTest {
    @Test
    void formatsMoneyUsingBrazilianSeparators() {
        assertEquals("$1.250.000,5", FormatUtil.money(1_250_000.50D));
    }

    @Test
    void clampsNegativeMoneyToZero() {
        assertEquals("$0", FormatUtil.money(-15D));
    }

    @Test
    void formatsDurationsWithoutSeconds() {
        assertEquals("<1m", FormatUtil.duration(59_999L));
        assertEquals("9h 17m", FormatUtil.duration((9 * 60L + 17L) * 60_000L));
    }
}
