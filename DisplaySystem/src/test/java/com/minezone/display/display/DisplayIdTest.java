package com.minezone.display.display;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayIdTest {
    @Test
    void resolvesEventDisplayFromAdminCommand() {
        DisplayId id = DisplayId.fromCommand("EVENTOS");

        assertEquals(DisplayId.EVENTOS, id);
        assertEquals("eventos", id.configPath());
        assertTrue(id.animated());
        assertFalse(id.isRanking());
    }
}
